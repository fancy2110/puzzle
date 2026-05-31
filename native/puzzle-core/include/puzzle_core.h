/* puzzle_core.h — C FFI header for puzzle image splitter library.
 *
 * Uses void* for the engine handle to simplify Kotlin/Native cinterop.
 */

#ifndef PUZZLE_CORE_H
#define PUZZLE_CORE_H

#include <stdint.h>

#ifdef __cplusplus
extern "C" {
#endif

/** Create an engine from in-memory PNG/JPEG bytes. Returns NULL on failure. */
void* puzzle_engine_new(const uint8_t* data, uint32_t len);

/**
 * Split the loaded image into irregular pieces.
 * Returns a JSON string (caller must free with puzzle_string_free).
 * Returns NULL on failure — check puzzle_last_error().
 */
char* puzzle_engine_split(void* handle, uint32_t piece_count, uint16_t block_size);

/**
 * Extract raw RGBA pixel data for a specific piece (0-indexed).
 * Returns actual bytes written, or 0 on error.
 * If buffer is too small, returns the required size without writing.
 */
uint32_t puzzle_engine_extract_pixels(
    void* handle, uint32_t piece_index,
    uint8_t* out_buffer, uint32_t buffer_capacity
);

/** Get the number of pieces from the last split result. */
uint32_t puzzle_engine_piece_count(void* handle);

/** Free the engine and all associated memory. */
void puzzle_engine_free(void* handle);

/** Free a string returned by the library. */
void puzzle_string_free(char* s);

/** Get the last error message. Valid until the next library call. */
const char* puzzle_last_error(void);

/**
 * Save all pieces from the last split as individual PNG files.
 * output_dir: absolute path to the output directory (will be created)
 * block_size: the block_size used during splitting (must match)
 * Returns a JSON array of saved filenames, or NULL on error.
 * Caller must free the string with puzzle_string_free().
 */
char* puzzle_engine_save_pieces(
    void* handle, const char* output_dir, uint32_t block_size
);

#ifdef __cplusplus
}
#endif

#endif /* PUZZLE_CORE_H */
