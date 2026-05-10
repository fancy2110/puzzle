package com.puzzle.game.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.puzzle.game.engine.model.PuzzlePiece
import com.puzzle.game.game.DragDropState

internal fun pieceImageScale(piece: PuzzlePiece, cardSize: Int = 70): Triple<Float, Float, Float> {
    val scaleX = cardSize.toFloat() / piece.pixels.width
    val scaleY = cardSize.toFloat() / piece.pixels.height
    val scale = minOf(scaleX, scaleY, 3f)
    val offX = -piece.pixels.left.toFloat() * scale
    val offY = -piece.pixels.top.toFloat() * scale
    return Triple(scale, offX, offY)
}

@Composable
internal fun PieceImageContent(
    piece: PuzzlePiece,
    puzzleBitmap: ImageBitmap?,
    cardSize: Int = 70,
    modifier: Modifier = Modifier
) {
    if (puzzleBitmap != null) {
        val (scale, offX, offY) = pieceImageScale(piece, cardSize)
        Image(
            bitmap = puzzleBitmap,
            contentDescription = "碎片",
            modifier = modifier
                .graphicsLayer(
                    scaleX = scale, scaleY = scale,
                    translationX = offX, translationY = offY
                ),
            contentScale = ContentScale.None
        )
    }
}

@Composable
fun PieceTray(
    pieces: List<PuzzlePiece>,
    puzzleBitmap: ImageBitmap?,
    placedPieceIds: Set<String>,
    dragState: DragDropState,
    onDragEnd: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 70.dp),
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        items(pieces, key = { it.id }) { piece ->
            val isPlaced = placedPieceIds.contains(piece.id)
            val isBeingDragged = dragState.draggedPieceId == piece.id && dragState.isDragging

            var pieceWindowPos by remember { mutableStateOf(Offset.Zero) }
            var pieceIntSize by remember { mutableStateOf(IntSize.Zero) }

            Box(
                modifier = Modifier
                    .size(70.dp)
                    .onGloballyPositioned { coords ->
                        pieceWindowPos = coords.positionInWindow()
                        pieceIntSize = coords.size
                    }
                    .then(
                        if (!isPlaced) {
                            Modifier.pointerInput(piece.id) {
                                detectDragGesturesAfterLongPress(
                                    onDragStart = { localOffset ->
                                        dragState.startDrag(
                                            pieceId = piece.id,
                                            startOffset = Offset(
                                                pieceWindowPos.x + localOffset.x,
                                                pieceWindowPos.y + localOffset.y
                                            ),
                                            pieceSize = Offset(
                                                pieceIntSize.width.toFloat(),
                                                pieceIntSize.height.toFloat()
                                            )
                                        )
                                    },
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        dragState.updateDrag(
                                            Offset(
                                                dragState.dragOffset.x + dragAmount.x,
                                                dragState.dragOffset.y + dragAmount.y
                                            )
                                        )
                                    },
                                    onDragEnd = { onDragEnd() },
                                    onDragCancel = { dragState.cancelDrag() }
                                )
                            }
                        } else Modifier
                    )
                    .alpha(if (isBeingDragged) 0.3f else 1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (isPlaced) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        else MaterialTheme.colorScheme.surface
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isPlaced) {
                    Text("✓", fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
                } else {
                    PieceImageContent(
                        piece = piece,
                        puzzleBitmap = puzzleBitmap,
                        cardSize = 70,
                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(6.dp))
                    )
                }
            }
        }
    }
}

@Composable
fun FloatingDraggedPiece(
    puzzleBitmap: ImageBitmap?,
    dragState: DragDropState,
    pieces: List<PuzzlePiece>
) {
    if (!dragState.isDragging || dragState.draggedPieceId == null) return
    val piece = pieces.firstOrNull { it.id == dragState.draggedPieceId } ?: return

    Box(
        modifier = Modifier
            .offset { IntOffset(dragState.dragOffset.x.toInt(), dragState.dragOffset.y.toInt()) }
            .size(80.dp)
            .shadow(8.dp, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        PieceImageContent(
            piece = piece,
            puzzleBitmap = puzzleBitmap,
            cardSize = 80,
            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(10.dp))
        )
    }
}
