package com.puzzle.game.engine

import kotlin.system.measureTimeMillis
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ImageSplitterPerformanceTest {
    @Test
    fun `splits 1024 square image into 1000 pixel pieces under 10 seconds`() {
        lateinit var pieces: List<com.puzzle.game.engine.model.PuzzlePiece>
        val elapsed = measureTimeMillis {
            val splitter = ImageSplitter(1024, 1024, PuzzleConfig.PIXEL_BLOCK_SIZE)
            val (_, result) = splitter.split(pieceCount = 1000, includeBlocks = false)
            pieces = result
        }

        assertEquals(1000, pieces.size)
        assertTrue(pieces.all { it.items.isEmpty() }, "Runtime split should not store per-pixel positions")
        assertTrue(pieces.all { it.outline.size >= 3 }, "Runtime pieces should keep an irregular render outline")
        assertTrue(pieces.all { it.pixels.width > 0 && it.pixels.height > 0 })
        assertTrue(elapsed < 10_000, "Expected split under 10s, actual ${elapsed}ms")
    }
}
