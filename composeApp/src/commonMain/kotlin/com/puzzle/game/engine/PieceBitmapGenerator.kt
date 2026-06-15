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

    private fun generatePixelPieceBitmap(
        sourcePixels: PixelMap,
        piece: PuzzlePiece
    ): ImageBitmap {
        val width = piece.pixels.width.coerceAtLeast(1)
        val height = piece.pixels.height.coerceAtLeast(1)
        val bitmap = ImageBitmap(width, height)
        val canvas = Canvas(bitmap)
        val paint = Paint()
        val mask = BooleanArray(width * height)

        for (pos in piece.items) {
            val localX = pos.x - piece.pixels.left
            val localY = pos.y - piece.pixels.top
            if (localX in 0 until width && localY in 0 until height) {
                mask[localY * width + localX] = true
            }
        }

        for (pos in piece.items) {
            val localX = pos.x - piece.pixels.left
            val localY = pos.y - piece.pixels.top
            if (localX !in 0 until width || localY !in 0 until height) continue

            val alpha = if (hasTransparentNeighbor(localX, localY, width, height, mask)) 0.55f else 1f
            paint.color = sourcePixels[pos.x, pos.y].withMultipliedAlpha(alpha)
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

    private fun hasTransparentNeighbor(
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        mask: BooleanArray
    ): Boolean {
        return !isMasked(x - 1, y, width, height, mask) ||
                !isMasked(x + 1, y, width, height, mask) ||
                !isMasked(x, y - 1, width, height, mask) ||
                !isMasked(x, y + 1, width, height, mask)
    }

    private fun isMasked(
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        mask: BooleanArray
    ): Boolean {
        if (x !in 0 until width || y !in 0 until height) return false
        return mask[y * width + x]
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
