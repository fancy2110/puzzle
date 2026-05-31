package com.puzzle.game.native

import com.puzzle.game.engine.model.PuzzlePiece
import com.puzzle.game.native.NativePuzzleEngine
import com.puzzle.logger.PuzzleLog

/**
 * Adapter that converts NativePuzzleEngine output into the existing
 * Kotlin PuzzlePiece / engine model, so the UI layer doesn't need to change.
 *
 * Usage:
 *   val adapter = NativeSplitAdapter()
 *   adapter.loadAndSplit(pngBytes, pieceCount = 12)
 *   val pieces: List<PuzzlePiece> = adapter.pieces
 *   val gridCols: Int = adapter.gridCols
 *   val gridRows: Int = adapter.gridRows
 *   val imageSize: Pair<Int, Int> = adapter.imageSize
 *   adapter.close()
 */
class NativeSplitAdapter {

    /** List of puzzle pieces in native split result format */
    var pieces: List<PuzzlePiece> = emptyList()
        private set

    var gridCols: Int = 0
        private set

    var gridRows: Int = 0
        private set

    /** (width, height) of the source image */
    var imageSize: Pair<Int, Int> = Pair(0, 0)
        private set

    /** Map of piece id → its center block position (y, x) */
    var correctPositions: Map<String, Pair<Int, Int>> = emptyMap()
        private set

    private var engine: NativePuzzleEngine? = null

    /**
     * Load a PNG/JPEG byte array and split it into puzzle pieces.
     *
     * @param imageBytes raw PNG or JPEG bytes
     * @param pieceCount desired number of pieces
     * @param blockSize grid cell size in pixels (default 64)
     * @return true on success
     */
    fun loadAndSplit(
        imageBytes: ByteArray,
        pieceCount: Int,
        blockSize: Int = 64
    ): Boolean {
        close()

        val eng = NativePuzzleEngine()
        if (!eng.isAvailable) {
            PuzzleLog.w("NativeSplit", "Native library not available")
            return false
        }

        if (!eng.loadImage(imageBytes)) {
            val err = eng.lastError() ?: "unknown error"
            eng.close()
            PuzzleLog.e("NativeSplit", "loadImage failed: $err")
            return false
        }

        val result = eng.split(pieceCount, blockSize)
        if (result == null) {
            val err = eng.lastError() ?: "unknown error"
            eng.close()
            PuzzleLog.e("NativeSplit", "Split failed: $err (image=${imageBytes.size/1024}KB pieces=$pieceCount)")
            return false
        }

        // Convert native pieces to PuzzlePiece model
        val puzzlePieces = result.pieces.map { nativePiece ->
            val pixelRect = com.puzzle.game.engine.model.Rect(
                left = nativePiece.pixel_left,
                top = nativePiece.pixel_top,
                right = nativePiece.pixel_left + nativePiece.pixel_width,
                bottom = nativePiece.pixel_top + nativePiece.pixel_height
            )

            val blockPositions = nativePiece.block_positions.map { bp ->
                com.puzzle.game.engine.model.Position(y = bp.y, x = bp.x)
            }

            val blockRect = if (blockPositions.isNotEmpty()) {
                com.puzzle.game.engine.model.Rect(
                    left = blockPositions.minOf { it.x },
                    top = blockPositions.minOf { it.y },
                    right = blockPositions.maxOf { it.x },
                    bottom = blockPositions.maxOf { it.y }
                )
            } else {
                com.puzzle.game.engine.model.Rect()
            }

            PuzzlePiece(
                id = nativePiece.id,
                pixels = pixelRect,
                blocks = blockRect,
                items = blockPositions.toMutableList(),
                isPlaced = false
            )
        }

        // Compute correct positions: center block of each piece
        val positions = mutableMapOf<String, Pair<Int, Int>>()
        for (piece in puzzlePieces) {
            if (piece.items.isNotEmpty()) {
                val center = piece.items[piece.items.size / 2]
                positions[piece.id] = Pair(center.y, center.x)
            }
        }

        pieces = puzzlePieces.shuffled()
        gridCols = result.grid_cols
        gridRows = result.grid_rows
        imageSize = Pair(result.image_width, result.image_height)
        correctPositions = positions
        engine = eng

        PuzzleLog.i("NativeSplit", "Split OK: ${result.image_width}×${result.image_height}px " +
            "grid=${result.grid_cols}×${result.grid_rows} bs=${result.block_size} " +
            "pieces=${puzzlePieces.size} totalBlocks=${result.grid_cols * result.grid_rows}")

        return true
    }

    /**
     * Extract raw RGBA pixel data for a specific piece.
     * Returns IntArray where each element is a byte value (0-255),
     * laid out as sequential [R, G, B, A, R, G, B, A, ...].
     */
    fun extractPixels(pieceIndex: Int): IntArray? {
        return engine?.extractPixels(pieceIndex)
    }

    /** Release native resources. Safe to call multiple times. */
    fun close() {
        engine?.close()
        engine = null
        pieces = emptyList()
    }
}
