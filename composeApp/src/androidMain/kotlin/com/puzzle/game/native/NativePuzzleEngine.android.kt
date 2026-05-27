package com.puzzle.game.native

import kotlinx.serialization.json.Json

/**
 * Android JNI wrapper for the Rust puzzle-core native library.
 *
 * Gracefully handles missing .so — set isAvailable=false and all methods return empty/null.
 * Build the .so: native/build_android.sh
 */
actual class NativePuzzleEngine actual constructor() {
    private var nativeHandle: Long = 0
    private val json = Json { ignoreUnknownKeys = true }

    actual val isAvailable: Boolean

    init {
        isAvailable = try {
            System.loadLibrary("puzzle_core")
            true
        } catch (e: UnsatisfiedLinkError) {
            false
        }
    }

    actual fun loadImage(data: ByteArray): Boolean {
        if (!isAvailable) return false
        val handle = nativeNew(data)
        if (handle == 0L) return false
        nativeHandle = handle
        return true
    }

    actual fun split(pieceCount: Int, blockSize: Int): NativeSplitResult? {
        if (!isAvailable || nativeHandle == 0L) return null
        val jsonStr = nativeSplit(nativeHandle, pieceCount, blockSize)
        return jsonStr?.let { json.decodeFromString(it) }
    }

    actual fun extractPixels(pieceIndex: Int): IntArray? {
        if (!isAvailable || nativeHandle == 0L) return null
        return nativeExtractPixels(nativeHandle, pieceIndex)
    }

    actual fun pieceCount(): Int {
        if (!isAvailable || nativeHandle == 0L) return 0
        return nativePieceCount(nativeHandle)
    }

    actual fun lastError(): String? {
        if (!isAvailable) return "Native library (libpuzzle_core.so) not available"
        return nativeLastError()
    }

    actual fun close() {
        if (isAvailable && nativeHandle != 0L) {
            nativeFree(nativeHandle)
            nativeHandle = 0
        }
    }

    protected fun finalize() {
        close()
    }

    // ── JNI native methods ──────────────────────────

    companion object {
        @JvmStatic private external fun nativeNew(data: ByteArray): Long
        @JvmStatic private external fun nativeSplit(handle: Long, pieceCount: Int, blockSize: Int): String?
        @JvmStatic private external fun nativeExtractPixels(handle: Long, pieceIndex: Int): IntArray?
        @JvmStatic private external fun nativePieceCount(handle: Long): Int
        @JvmStatic private external fun nativeLastError(): String?
        @JvmStatic private external fun nativeFree(handle: Long)
    }
}
