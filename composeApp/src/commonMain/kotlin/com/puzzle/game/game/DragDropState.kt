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
    var dropTargetPieceId: String? by mutableStateOf(null)
    var dropOverlapRatio: Float by mutableStateOf(0f)

    /** Tap-to-select mode: simpler interaction for kids */
    var selectedPieceId: String? by mutableStateOf(null)

    fun tapSelect(pieceId: String) {
        if (selectedPieceId == pieceId) {
            selectedPieceId = null
        } else {
            selectedPieceId = pieceId
        }
    }

    fun tapTarget(targetPieceId: String): DragResult? {
        val pieceId = selectedPieceId ?: return null
        selectedPieceId = null
        return DragResult(pieceId, targetPieceId)
    }

    fun clearSelection() {
        selectedPieceId = null
    }

    fun startDrag(pieceId: String, startOffset: Offset, pieceSize: Offset) {
        draggedPieceId = pieceId
        dragOffset = startOffset
        dragPieceSize = pieceSize
        isDragging = true
        selectedPieceId = null
    }

    fun updateDrag(offset: Offset) {
        if (dragOffset != offset) {
            dragOffset = offset
        }
    }

    fun updateDropTarget(pieceId: String?, overlapRatio: Float = 0f) {
        if (dropTargetPieceId != pieceId || kotlin.math.abs(dropOverlapRatio - overlapRatio) > 0.02f) {
            dropTargetPieceId = pieceId
            dropOverlapRatio = overlapRatio
        }
    }

    fun endDrag(): DragResult? {
        val pieceId = draggedPieceId
        val target = dropTargetPieceId
        isDragging = false
        draggedPieceId = null
        dropTargetCell = null
        dropTargetPieceId = null
        dropOverlapRatio = 0f

        if (pieceId != null && target != null) {
            return DragResult(pieceId, target)
        }
        return null
    }

    fun cancelDrag() {
        isDragging = false
        draggedPieceId = null
        dropTargetCell = null
        dropTargetPieceId = null
        dropOverlapRatio = 0f
    }
}

data class DragResult(
    val pieceId: String,
    val targetPieceId: String
)
