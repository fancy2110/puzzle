package com.puzzle.game.engine

import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PixelMap
import androidx.compose.ui.graphics.toPixelMap
import com.puzzle.game.engine.model.PuzzlePiece

object PieceBitmapGenerator {
    fun generate(
        source: ImageBitmap,
        pieces: List<PuzzlePiece>,
        blockSize: Int
    ): Map<String, ImageBitmap> {
        if (pieces.isEmpty()) return emptyMap()
        val pixels = source.toPixelMap()
        return pieces.associate { piece ->
            piece.id to generatePieceBitmap(
                sourcePixels = pixels,
                piece = piece,
                blockSize = blockSize
            )
        }
    }

    private fun generatePieceBitmap(
        sourcePixels: PixelMap,
        piece: PuzzlePiece,
        blockSize: Int
    ): ImageBitmap {
        if (blockSize == PuzzleConfig.PIXEL_BLOCK_SIZE) {
            return generatePixelPieceBitmap(sourcePixels, piece)
        }

        val width = piece.pixels.width.coerceAtLeast(1)
        val height = piece.pixels.height.coerceAtLeast(1)
        val bitmap = ImageBitmap(width, height)
        val canvas = Canvas(bitmap)
        val paint = Paint()
        val ownedBlocks = piece.items.map { it.y to it.x }.toHashSet()

        for (y in 0 until height) {
            for (x in 0 until width) {
                val sourceX = piece.pixels.left + x
                val sourceY = piece.pixels.top + y
                val blockX = sourceX / blockSize
                val blockY = sourceY / blockSize
                if (!ownedBlocks.contains(blockY to blockX)) continue

                paint.color = sourcePixels[sourceX, sourceY]
                canvas.drawRect(
                    left = x.toFloat(),
                    top = y.toFloat(),
                    right = x + 1f,
                    bottom = y + 1f,
                    paint = paint
                )
            }
        }

        return bitmap
    }

    private fun generatePixelPieceBitmap(
        sourcePixels: PixelMap,
        piece: PuzzlePiece
    ): ImageBitmap {
        val width = piece.pixels.width.coerceAtLeast(1)
        val height = piece.pixels.height.coerceAtLeast(1)
        val bitmap = ImageBitmap(width, height)
        val canvas = Canvas(bitmap)
        val paint = Paint()

        for (pos in piece.items) {
            val localX = pos.x - piece.pixels.left
            val localY = pos.y - piece.pixels.top
            if (localX !in 0 until width || localY !in 0 until height) continue

            paint.color = sourcePixels[pos.x, pos.y]
            canvas.drawRect(
                left = localX.toFloat(),
                top = localY.toFloat(),
                right = localX + 1f,
                bottom = localY + 1f,
                paint = paint
            )
        }

        return bitmap
    }
}
