package com.puzzle.game.game

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset

class DragDropState {
    var draggedPieceId: String? by mutableStateOf(null)
    var dragOffset by mutableStateOf(Offset.Zero)
    var dragPieceSize by mutableStateOf(Offset.Zero)
    var isDragging by mutableStateOf(false)
    var dropTargetCell: Pair<Int, Int>? by mutableStateOf(null)

    fun startDrag(pieceId: String, startOffset: Offset, pieceSize: Offset) {
        draggedPieceId = pieceId
        dragOffset = startOffset
        dragPieceSize = pieceSize
        isDragging = true
    }

    fun updateDrag(offset: Offset) {
        dragOffset = offset
    }

    fun endDrag(): DragResult? {
        val pieceId = draggedPieceId
        val target = dropTargetCell
        isDragging = false
        draggedPieceId = null
        dropTargetCell = null

        if (pieceId != null && target != null) {
            return DragResult(pieceId, target.first, target.second)
        }
        return null
    }

    fun cancelDrag() {
        isDragging = false
        draggedPieceId = null
        dropTargetCell = null
    }
}

data class DragResult(
    val pieceId: String,
    val targetRow: Int,
    val targetCol: Int
)
