package com.puzzle.game.native

import kotlinx.serialization.Serializable

@Serializable
data class NativePieceData(
    val id: String,
    val pixel_left: Int,
    val pixel_top: Int,
    val pixel_width: Int,
    val pixel_height: Int,
    val block_positions: List<NativeBlockPosition>
)

@Serializable
data class NativeBlockPosition(
    val y: Int,
    val x: Int
)

@Serializable
data class NativeSplitResult(
    val image_width: Int,
    val image_height: Int,
    val grid_cols: Int,
    val grid_rows: Int,
    val block_size: Int,
    val pieces: List<NativePieceData>
)

/**
 * Cross-platform wrapper around the native Rust puzzle-core library.
 *
 * Android: JNI (libpuzzle_core.so) — may be unavailable if .so not built
 * iOS:     cinterop (PuzzleCore.xcframework or stub)
 */
expect class NativePuzzleEngine() {

    /** Whether the native library loaded successfully. */
    val isAvailable: Boolean

    fun loadImage(data: ByteArray): Boolean
    fun split(pieceCount: Int, blockSize: Int = 64): NativeSplitResult?
    fun extractPixels(pieceIndex: Int): IntArray?
    fun pieceCount(): Int
    fun lastError(): String?
    fun close()

    /**
     * Save all pieces from the last split as individual PNG files.
     * @param outputDir absolute path to the output directory
     * @param blockSize the block size used during splitting (must match)
     * @return JSON array of saved filenames, or null on error
     */
    fun savePieces(outputDir: String, blockSize: Int = 64): String?
}
