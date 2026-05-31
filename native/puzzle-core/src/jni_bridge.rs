//! JNI bridge — maps Kotlin native method calls to Rust FFI functions.
//! All JNI functions are compiled directly into libpuzzle_core.so.

use jni::JNIEnv;
use jni::objects::{JByteArray, JClass, JString};
use jni::sys::{jbyteArray, jint, jintArray, jlong, jstring};

use crate::ffi::{
    puzzle_engine_extract_pixels, puzzle_engine_free, puzzle_engine_new,
    puzzle_engine_piece_count, puzzle_engine_split, puzzle_last_error,
    puzzle_string_free,
};

// ── nativeNew(byte[] data) → long ────────────────────────

#[no_mangle]
pub extern "system" fn Java_com_puzzle_game_native_NativePuzzleEngine_nativeNew<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    data: JByteArray<'local>,
) -> jlong {
    let len = env.get_array_length(&data).unwrap_or(0) as usize;
    if len == 0 {
        return 0;
    }

    let mut buf = vec![0i8; len];
    if env.get_byte_array_region(&data, 0, &mut buf).is_err() {
        return 0;
    }

    let handle = puzzle_engine_new(buf.as_ptr() as *const u8, len as u32);
    handle as jlong
}

// ── nativeSplit(long handle, int pieceCount, int blockSize) → String ──

#[no_mangle]
pub extern "system" fn Java_com_puzzle_game_native_NativePuzzleEngine_nativeSplit<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    handle: jlong,
    piece_count: jint,
    block_size: jint,
) -> jstring {
    let json_ptr = puzzle_engine_split(handle as _, piece_count as u32, block_size as u16);

    if json_ptr.is_null() {
        return std::ptr::null_mut();
    }

    let c_str = unsafe { std::ffi::CStr::from_ptr(json_ptr) };
    let result = env.new_string(c_str.to_str().unwrap_or("")).ok();
    unsafe { puzzle_string_free(json_ptr) };
    result.map(|s| s.into_raw()).unwrap_or(std::ptr::null_mut())
}

// ── nativeExtractPixels(long handle, int pieceIndex) → int[] ──

#[no_mangle]
pub extern "system" fn Java_com_puzzle_game_native_NativePuzzleEngine_nativeExtractPixels<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    handle: jlong,
    piece_index: jint,
) -> jintArray {
    let needed = puzzle_engine_extract_pixels(handle as _, piece_index as u32, std::ptr::null_mut(), 0)
        as usize;

    if needed == 0 {
        return std::ptr::null_mut();
    }

    let mut buf = vec![0u8; needed];
    let written = puzzle_engine_extract_pixels(
        handle as _,
        piece_index as u32,
        buf.as_mut_ptr(),
        needed as u32,
    ) as usize;

    if written != needed {
        return std::ptr::null_mut();
    }

    // Convert u8 Vec to jint array
    let ints: Vec<jint> = buf.iter().map(|&b| b as jint).collect();
    match env.new_int_array(ints.len() as i32) {
        Ok(arr) => {
            let _ = env.set_int_array_region(&arr, 0, &ints);
            arr.into_raw()
        }
        Err(_) => std::ptr::null_mut(),
    }
}

// ── nativePieceCount(long handle) → int ──────────────────

#[no_mangle]
pub extern "system" fn Java_com_puzzle_game_native_NativePuzzleEngine_nativePieceCount(
    _env: JNIEnv,
    _class: JClass,
    handle: jlong,
) -> jint {
    puzzle_engine_piece_count(handle as _) as jint
}

// ── nativeLastError() → String ───────────────────────────

#[no_mangle]
pub extern "system" fn Java_com_puzzle_game_native_NativePuzzleEngine_nativeLastError<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
) -> jstring {
    let err = puzzle_last_error();
    if err.is_null() {
        return std::ptr::null_mut();
    }
    let c_str = unsafe { std::ffi::CStr::from_ptr(err) };
    env.new_string(c_str.to_str().unwrap_or(""))
        .ok()
        .map(|s| s.into_raw())
        .unwrap_or(std::ptr::null_mut())
}

// ── nativeFree(long handle) ──────────────────────────────

#[no_mangle]
pub extern "system" fn Java_com_puzzle_game_native_NativePuzzleEngine_nativeFree(
    _env: JNIEnv,
    _class: JClass,
    handle: jlong,
) {
    puzzle_engine_free(handle as _);
}
