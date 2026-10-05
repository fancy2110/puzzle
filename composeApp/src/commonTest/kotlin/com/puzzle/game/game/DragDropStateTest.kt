package com.puzzle.game.game

import androidx.compose.ui.geometry.Offset
import kotlin.test.Test
import kotlin.test.assertEquals

class DragDropStateTest {
    @Test
    fun draggedPieceKeepsOriginalTouchAnchor() {
        val state = DragDropState()

        state.startDrag(
            pieceId = "piece-1",
            startOffset = Offset(120f, 640f),
            pieceSize = Offset(116f, 122f),
            touchOffset = Offset(24f, 36f)
        )

        assertEquals(Offset(120f, 640f), state.dragOffset)

        state.updateDragPointer(Offset(224f, 476f))

        assertEquals(Offset(200f, 440f), state.dragOffset)
        assertEquals(Offset(116f, 122f), state.dragPieceSize)
    }
}
