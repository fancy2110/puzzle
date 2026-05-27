/* stub_puzzle_core.c — Stub implementation for development without Rust toolchain.
 *
 * Provides all puzzle_core C FFI symbols so that the Kotlin/Native cinterop
 * always compiles. Returns error/empty results for all operations.
 */

#include <stdint.h>
#include <stdlib.h>

void* puzzle_engine_new(const uint8_t* data, uint32_t len) {
    return NULL; /* stub: no image processing */
}

char* puzzle_engine_split(void* handle, uint32_t piece_count, uint16_t block_size) {
    return NULL;
}

uint32_t puzzle_engine_extract_pixels(
    void* handle, uint32_t piece_index,
    uint8_t* out_buffer, uint32_t buffer_capacity) {
    return 0;
}

uint32_t puzzle_engine_piece_count(void* handle) {
    return 0;
}

void puzzle_engine_free(void* handle) {
    /* nothing to free in stub */
}

void puzzle_string_free(char* s) {
    if (s) free(s);
}

const char* puzzle_last_error(void) {
    return "Native library not built — run native/build_ios.sh";
}
