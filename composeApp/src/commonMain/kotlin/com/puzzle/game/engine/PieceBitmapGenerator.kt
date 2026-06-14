package com.puzzle.game.engine

import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
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

                val alpha = edgeAlpha(
                    sourceX = sourceX,
                    sourceY = sourceY,
                    blockSize = blockSize,
                    ownedBlocks = ownedBlocks,
                    sourceWidth = sourcePixels.width,
                    sourceHeight = sourcePixels.height
                )
                if (alpha <= 0f) continue

                paint.color = sourcePixels[sourceX, sourceY].withMultipliedAlpha(alpha)
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

    private fun edgeAlpha(
        sourceX: Int,
        sourceY: Int,
        blockSize: Int,
        ownedBlocks: Set<Pair<Int, Int>>,
        sourceWidth: Int,
        sourceHeight: Int
    ): Float {
        val hasTransparentNeighbor =
            !isOwnedPixel(sourceX - 1, sourceY, blockSize, ownedBlocks, sourceWidth, sourceHeight) ||
                    !isOwnedPixel(sourceX + 1, sourceY, blockSize, ownedBlocks, sourceWidth, sourceHeight) ||
                    !isOwnedPixel(sourceX, sourceY - 1, blockSize, ownedBlocks, sourceWidth, sourceHeight) ||
                    !isOwnedPixel(sourceX, sourceY + 1, blockSize, ownedBlocks, sourceWidth, sourceHeight)
        return if (hasTransparentNeighbor) 0.55f else 1f
    }

    private fun isOwnedPixel(
        x: Int,
        y: Int,
        blockSize: Int,
        ownedBlocks: Set<Pair<Int, Int>>,
        sourceWidth: Int,
        sourceHeight: Int
    ): Boolean {
        if (x !in 0 until sourceWidth || y !in 0 until sourceHeight) return false
        return ownedBlocks.contains((y / blockSize) to (x / blockSize))
    }

    private fun Color.withMultipliedAlpha(multiplier: Float): Color {
        return copy(alpha = alpha * multiplier.coerceIn(0f, 1f))
    }
}
