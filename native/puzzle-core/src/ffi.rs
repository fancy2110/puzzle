//! C FFI exports for puzzle-core.
//!
//! All functions are `extern "C"` and `#[no_mangle]`.
//! Thread safety: not supported — call from a single thread.

use crate::engine::{PuzzleEngine, SplitResult};
use serde_json;
use std::ffi::CString;
use std::os::raw::c_char;

// ── Opaque handle ───────────────────────────────────────

/// Opaque pointer to a PuzzleEngine instance.
pub struct PuzzleEngineHandle {
    engine: PuzzleEngine,
    /// Cached split result (so caller can query pieces after split)
    result: Option<SplitResult>,
}

// ── Public API ──────────────────────────────────────────

/// Create an engine from in-memory PNG/JPEG bytes.
///
/// Returns null on failure. Check `puzzle_last_error()` for details.
#[no_mangle]
pub extern "C" fn puzzle_engine_new(
    data: *const u8,
    len: u32,
) -> *mut PuzzleEngineHandle {
    if data.is_null() || len == 0 {
        set_error("null data pointer or zero length");
        return std::ptr::null_mut();
    }

    let bytes = unsafe { std::slice::from_raw_parts(data, len as usize) };
    match PuzzleEngine::from_bytes(bytes) {
        Ok(engine) => {
            let handle = Box::new(PuzzleEngineHandle {
                engine,
                result: None,
            });
            Box::into_raw(handle)
        }
        Err(e) => {
            set_error(&e);
            std::ptr::null_mut()
        }
    }
}

/// Split the loaded image into irregular pieces.
///
/// Returns a JSON string with piece data. The caller owns the string
/// and must free it with `puzzle_string_free()`.
/// Returns null on failure.
///
/// JSON schema: see SplitResult in engine.rs
#[no_mangle]
pub extern "C" fn puzzle_engine_split(
    handle: *mut PuzzleEngineHandle,
    piece_count: u32,
    block_size: u16,
) -> *mut c_char {
    let h = match unsafe { handle.as_mut() } {
        Some(h) => h,
        None => {
            set_error("null handle");
            return std::ptr::null_mut();
        }
    };

    let result = h.engine.split(piece_count as usize, block_size);
    let json = match serde_json::to_string(&result) {
        Ok(j) => j,
        Err(e) => {
            set_error(&format!("JSON serialization failed: {}", e));
            return std::ptr::null_mut();
        }
    };

    h.result = Some(result);

    match CString::new(json) {
        Ok(cs) => cs.into_raw(),
        Err(_) => {
            set_error("string contains null byte");
            std::ptr::null_mut()
        }
    }
}

/// Extract raw RGBA pixel data for a specific piece (by index).
///
/// The output buffer layout: sequential RGBA bytes (width * height * 4 bytes).
/// The caller must provide a buffer of sufficient size.
/// Returns the number of bytes written, or 0 on error.
#[no_mangle]
pub extern "C" fn puzzle_engine_extract_pixels(
    handle: *mut PuzzleEngineHandle,
    piece_index: u32,
    out_buffer: *mut u8,
    buffer_capacity: u32,
) -> u32 {
    let h = match unsafe { handle.as_mut() } {
        Some(h) => h,
        None => return 0,
    };

    let result = match &h.result {
        Some(r) => r,
        None => {
            set_error("no split result — call puzzle_engine_split first");
            return 0;
        }
    };

    let piece = match result.pieces.get(piece_index as usize) {
        Some(p) => p,
        None => {
            set_error("piece index out of bounds");
            return 0;
        }
    };

    let pixels = h.engine.extract_piece_pixels(piece);
    let needed = pixels.len() as u32;

    if needed > buffer_capacity {
        set_error("output buffer too small");
        return needed; // still report needed size
    }

    unsafe {
        std::ptr::copy_nonoverlapping(pixels.as_ptr(), out_buffer, pixels.len());
    }

    needed
}

/// Get the number of pieces from the last split result.
#[no_mangle]
pub extern "C" fn puzzle_engine_piece_count(handle: *mut PuzzleEngineHandle) -> u32 {
    let h = match unsafe { handle.as_mut() } {
        Some(h) => h,
        None => return 0,
    };

    h.result.as_ref().map(|r| r.pieces.len() as u32).unwrap_or(0)
}

/// Free the engine and all associated memory.
#[no_mangle]
pub extern "C" fn puzzle_engine_free(handle: *mut PuzzleEngineHandle) {
    if !handle.is_null() {
        unsafe { drop(Box::from_raw(handle)); }
    }
}

/// Free a string returned by any puzzle_engine_* function.
#[no_mangle]
pub extern "C" fn puzzle_string_free(s: *mut c_char) {
    if !s.is_null() {
        unsafe { drop(CString::from_raw(s)); }
    }
}

/// Save pieces from the last split as PNG files.
/// Returns a JSON array of filenames (caller must puzzle_string_free).
#[no_mangle]
pub extern "C" fn puzzle_engine_save_pieces(
    handle: *mut PuzzleEngineHandle,
    output_dir: *const c_char,
    block_size: u32,
) -> *mut c_char {
    let h = match unsafe { handle.as_mut() } {
        Some(h) => h,
        None => {
            set_error("null handle");
            return std::ptr::null_mut();
        }
    };

    let result = match &h.result {
        Some(r) => r,
        None => {
            set_error("no split result — call puzzle_engine_split first");
            return std::ptr::null_mut();
        }
    };

    let dir_str = match unsafe { std::ffi::CStr::from_ptr(output_dir) }.to_str() {
        Ok(s) => s,
        Err(_) => {
            set_error("invalid UTF-8 in output_dir");
            return std::ptr::null_mut();
        }
    };

    let path = std::path::Path::new(dir_str);
    match h.engine.save_pieces(&result.pieces, block_size, path) {
        Ok(filenames) => {
            let json = serde_json::to_string(&filenames).unwrap_or_else(|_| "[]".to_string());
            CString::new(json).ok().map(|cs| cs.into_raw()).unwrap_or(std::ptr::null_mut())
        }
        Err(e) => {
            set_error(&e);
            std::ptr::null_mut()
        }
    }
}

/// Get the last error message. Returns null if no error.
/// The returned pointer is valid until the next call into the library.
#[no_mangle]
pub extern "C" fn puzzle_last_error() -> *const c_char {
    LAST_ERROR.with(|cell| {
        cell.borrow().as_ref().map(|s| s.as_ptr()).unwrap_or(std::ptr::null())
    })
}

// ── Thread-local error storage ─────────────────────────

use std::cell::RefCell;

thread_local! {
    static LAST_ERROR: RefCell<Option<CString>> = RefCell::new(None);
}

fn set_error(msg: &str) {
    if let Ok(cs) = CString::new(msg) {
        LAST_ERROR.with(|cell| {
            *cell.borrow_mut() = Some(cs);
        });
    }
}
