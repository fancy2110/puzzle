/* jni_glue.c — JNI bindings for puzzle-core Rust library.
 *
 * Compile with Android NDK:
 *   $CC -shared -I$JAVA_HOME/include -I$JAVA_HOME/include/darwin \
 *       -I../puzzle-core/include -L../puzzle-core/target/<target>/release \
 *       -lpuzzle_core -o libpuzzle_core_jni.so jni_glue.c
 *
 * Or better: use cargo-ndk which handles this automatically.
 * The JNI functions below map JVM calls to the Rust C FFI.
 */

#include <jni.h>
#include <string.h>
#include "puzzle_core.h"

// ── Helpers ──────────────────────────────────────────────

static jstring make_jstring(JNIEnv *env, const char *str) {
    if (str == NULL) return NULL;
    return (*env)->NewStringUTF(env, str);
}

static jintArray make_int_array(JNIEnv *env, const uint8_t *data, jsize len) {
    if (data == NULL || len <= 0) return NULL;
    jintArray arr = (*env)->NewIntArray(env, len);
    if (arr == NULL) return NULL;
    jint *buf = (*env)->GetIntArrayElements(env, arr, NULL);
    for (jsize i = 0; i < len; i++) {
        buf[i] = (jint)(data[i] & 0xFF);
    }
    (*env)->ReleaseIntArrayElements(env, arr, buf, 0);
    return arr;
}

// ── JNI Method Implementations ──────────────────────────

JNIEXPORT jlong JNICALL
Java_com_puzzle_game_native_NativePuzzleEngine_nativeNew(
    JNIEnv *env, jclass clazz, jbyteArray data) {

    jsize len = (*env)->GetArrayLength(env, data);
    if (len <= 0) return 0;

    jbyte *bytes = (*env)->GetByteArrayElements(env, data, NULL);
    if (bytes == NULL) return 0;

    PuzzleEngineHandle *handle = puzzle_engine_new((const uint8_t *)bytes, (uint32_t)len);
    (*env)->ReleaseByteArrayElements(env, data, bytes, JNI_ABORT);
    return (jlong)(uintptr_t)handle;
}

JNIEXPORT jstring JNICALL
Java_com_puzzle_game_native_NativePuzzleEngine_nativeSplit(
    JNIEnv *env, jclass clazz, jlong handle,
    jint pieceCount, jint blockSize) {

    char *json = puzzle_engine_split(
        (PuzzleEngineHandle *)(uintptr_t)handle,
        (uint32_t)pieceCount,
        (uint16_t)blockSize
    );

    if (json == NULL) return NULL;

    jstring result = (*env)->NewStringUTF(env, json);
    puzzle_string_free(json);
    return result;
}

JNIEXPORT jintArray JNICALL
Java_com_puzzle_game_native_NativePuzzleEngine_nativeExtractPixels(
    JNIEnv *env, jclass clazz, jlong handle, jint pieceIndex) {

    PuzzleEngineHandle *h = (PuzzleEngineHandle *)(uintptr_t)handle;

    // First call to get required buffer size
    uint32_t needed = puzzle_engine_extract_pixels(h, (uint32_t)pieceIndex, NULL, 0);
    if (needed == 0) return NULL;

    uint8_t *buffer = (uint8_t *)malloc(needed);
    if (buffer == NULL) return NULL;

    uint32_t written = puzzle_engine_extract_pixels(
        h, (uint32_t)pieceIndex, buffer, needed);

    jintArray result = NULL;
    if (written == needed) {
        result = make_int_array(env, buffer, (jsize)written);
    }

    free(buffer);
    return result;
}

JNIEXPORT jint JNICALL
Java_com_puzzle_game_native_NativePuzzleEngine_nativePieceCount(
    JNIEnv *env, jclass clazz, jlong handle) {

    return (jint)puzzle_engine_piece_count(
        (PuzzleEngineHandle *)(uintptr_t)handle);
}

JNIEXPORT jstring JNICALL
Java_com_puzzle_game_native_NativePuzzleEngine_nativeLastError(
    JNIEnv *env, jclass clazz) {

    const char *err = puzzle_last_error();
    return make_jstring(env, err);
}

JNIEXPORT void JNICALL
Java_com_puzzle_game_native_NativePuzzleEngine_nativeFree(
    JNIEnv *env, jclass clazz, jlong handle) {

    puzzle_engine_free((PuzzleEngineHandle *)(uintptr_t)handle);
}
