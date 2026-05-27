@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.puzzle.game.native

import kotlinx.cinterop.*
import kotlinx.serialization.json.Json
import puzzle_core.*

/**
 * iOS cinterop wrapper for the Rust puzzle-core static library.
 */
actual class NativePuzzleEngine actual constructor() {
    private var nativeHandle: COpaquePointer? = null
    private val json = Json { ignoreUnknownKeys = true }

    /** On iOS, the stub is always linked so the library is available. */
    actual val isAvailable: Boolean = true

    actual fun loadImage(data: ByteArray): Boolean {
        val handle = data.usePinned { pinned ->
            puzzle_engine_new(
                pinned.addressOf(0).reinterpret(),
                data.size.toUInt()
            )
        }
        if (handle == null) return false
        nativeHandle = handle
        return true
    }

    actual fun split(pieceCount: Int, blockSize: Int): NativeSplitResult? {
        val h = nativeHandle ?: return null
        val jsonStr = puzzle_engine_split(
            h, pieceCount.toUInt(), blockSize.toUShort()
        )?.toKString()
        return jsonStr?.let { json.decodeFromString(it) }
    }

    actual fun extractPixels(pieceIndex: Int): IntArray? {
        val h = nativeHandle ?: return null
        val needed = puzzle_engine_extract_pixels(
            h, pieceIndex.toUInt(), null, 0u
        ).toInt()
        if (needed == 0) return null

        return memScoped {
            val buffer = allocArray<UByteVar>(needed)
            val written = puzzle_engine_extract_pixels(
                h, pieceIndex.toUInt(), buffer, needed.toUInt()
            ).toInt()
            if (written != needed) return@memScoped null
            IntArray(needed) { i -> buffer[i].toInt() and 0xFF }
        }
    }

    actual fun pieceCount(): Int {
        val h = nativeHandle ?: return 0
        return puzzle_engine_piece_count(h).toInt()
    }

    actual fun lastError(): String? {
        return puzzle_last_error()?.toKString()
    }

    actual fun close() {
        nativeHandle?.let {
            puzzle_engine_free(it)
            nativeHandle = null
        }
    }
}
