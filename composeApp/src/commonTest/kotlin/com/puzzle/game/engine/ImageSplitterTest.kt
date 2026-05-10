package com.puzzle.game.engine

import com.puzzle.game.engine.model.PuzzlePiece
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ImageSplitterTest {

    @Test
    fun `split produces correct piece count`() {
        val splitter = ImageSplitter(800, 600, 64)
        val (blocks, pieces) = splitter.split(6)

        assertEquals(6, pieces.size, "Should produce exactly 6 pieces")
        assertTrue(blocks.isNotEmpty(), "Should have blocks")
        assertTrue(blocks.all { row -> row.isNotEmpty() }, "All rows should have blocks")
    }

    @Test
    fun `split covers all blocks`() {
        val splitter = ImageSplitter(800, 600, 64)
        val (blocks, pieces) = splitter.split(12)

        val totalBlocks = blocks.sumOf { it.size }
        val coveredPositions = pieces.flatMap { it.items }.toSet()

        assertEquals(totalBlocks, coveredPositions.size,
            "Every block should be assigned to exactly one piece")
    }

    @Test
    fun `split pieces have no overlapping blocks`() {
        val splitter = ImageSplitter(800, 600, 64)
        val (_, pieces) = splitter.split(20)

        for (i in pieces.indices) {
            for (j in i + 1 until pieces.size) {
                val overlap = pieces[i].items.any { a ->
                    pieces[j].items.any { b -> a == b }
                }
                assertTrue(!overlap, "Pieces $i and $j should not overlap")
            }
        }
    }

    @Test
    fun `split handles minimum piece count`() {
        val splitter = ImageSplitter(800, 600, 64)
        val (_, pieces) = splitter.split(1)
        assertEquals(1, pieces.size)
    }

    @Test
    fun `split handles piece count larger than blocks`() {
        val splitter = ImageSplitter(100, 100, 64)
        val totalPossible = ((100 / 64) + 1) * ((100 / 64) + 1)
        val (_, pieces) = splitter.split(100)
        assertTrue(pieces.size <= totalPossible,
            "Should not produce more pieces than available blocks")
    }

    @Test
    fun `split produces acceptable piece area variance`() {
        val splitter = ImageSplitter(800, 600, 64)
        val (_, pieces) = splitter.split(6)

        val areas = pieces.map { it.items.size }
        val avg = areas.average()
        val maxDeviation = areas.maxOf { kotlin.math.abs(it - avg) / avg * 100 }

        assertTrue(maxDeviation < 200, "Area deviation should be under 200%, got $maxDeviation%")
    }

    @Test
    fun `piece model correctly tracks boundaries`() {
        val splitter = ImageSplitter(800, 600, 64)
        val (blocks, pieces) = splitter.split(6)

        for (piece in pieces) {
            assertTrue(piece.pixels.width > 0, "Piece should have positive width")
            assertTrue(piece.pixels.height > 0, "Piece should have positive height")
            assertTrue(piece.items.isNotEmpty(), "Piece should have at least one block")
        }
    }

    @Test
    fun `blocks are correctly positioned in grid`() {
        val splitter = ImageSplitter(800, 600, 64)
        val (blocks, pieces) = splitter.split(4)

        val cols = (800 / 64) + 1
        val rows = (600 / 64) + 1

        assertEquals(rows, blocks.size, "Grid rows should match")
        assertTrue(blocks.all { row -> row.size == cols }, "All rows should have same column count")
    }
}
