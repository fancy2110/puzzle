package com.puzzle.game.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.puzzle.game.engine.model.PuzzlePiece
import com.puzzle.game.game.DragDropState

// ── Piece image renderer ─────────────────────────────────

/**
 * Scale such that each grid block is at least [minBlockDp] dp tall in the card.
 * This prevents the content from becoming microscopic for pieces with large
 * bounding boxes but sparse blocks (a side effect of BFS irregular splitting).
 */
internal fun pieceImageScale(piece: PuzzlePiece, cardSize: Int, blockSizePx: Int = 64, minBlockDp: Float = 14f): Triple<Float, Float, Float> {
    val w = piece.pixels.width.coerceAtLeast(1)
    val h = piece.pixels.height.coerceAtLeast(1)
    val bbScale = minOf(cardSize.toFloat() / w, cardSize.toFloat() / h)
    val blockScale = minBlockDp / blockSizePx.toFloat()
    val scale = maxOf(bbScale, blockScale).coerceAtMost(8f)
    val offX = -piece.pixels.left.toFloat() * scale
    val offY = -piece.pixels.top.toFloat() * scale
    return Triple(scale, offX, offY)
}

/**
 * Renders a puzzle piece as a rectangular crop from the source bitmap.
 * The scale is computed to ensure each block is at least [minBlockDp] dp,
 * so the piece's content fills the card even when the splitting algorithm
 * produces sparse irregular shapes.
 */
@Composable
internal fun PieceImageContent(
    piece: PuzzlePiece,
    puzzleBitmap: ImageBitmap?,
    cardSize: Int = 70,
    blockSizePx: Int = 64,
    minBlockDp: Float = 14f,
    modifier: Modifier = Modifier
) {
    if (puzzleBitmap == null || piece.items.isEmpty()) return

    val (scale, offX, offY) = remember(piece.id, cardSize) {
        pieceImageScale(piece, cardSize, blockSizePx, minBlockDp)
    }

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

@Composable
fun PieceTray(
    pieces: List<PuzzlePiece>,
    puzzleBitmap: ImageBitmap?,
    placedPieceIds: Set<String>,
    dragState: DragDropState,
    onDragEnd: () -> Unit,
    onTapPiece: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 100.dp),
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        items(pieces, key = { it.id }) { piece ->
            val isPlaced = placedPieceIds.contains(piece.id)
            val isBeingDragged = dragState.draggedPieceId == piece.id && dragState.isDragging
            val isSelected = dragState.selectedPieceId == piece.id

            var pieceWindowPos by remember { mutableStateOf(Offset.Zero) }
            var pieceIntSize by remember { mutableStateOf(IntSize.Zero) }

            Box(
                modifier = Modifier
                    .size(100.dp)
                    .onGloballyPositioned { coords ->
                        pieceWindowPos = coords.positionInWindow()
                        pieceIntSize = coords.size
                    }
                    .then(
                        if (!isPlaced) {
                            Modifier.pointerInput(piece.id) {
                                detectDragGestures(
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
                    .then(
                        if (isSelected) {
                            Modifier.border(3.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
                        } else Modifier
                    )
                    .background(
                        when {
                            isPlaced -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                            isSelected -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                            else -> MaterialTheme.colorScheme.surface
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isPlaced) {
                    Text("✓", fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
                } else {
                    PieceImageContent(
                        piece = piece,
                        puzzleBitmap = puzzleBitmap,
                        cardSize = 100,
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
    pieceBitmaps: Map<String, ImageBitmap> = emptyMap(),
    dragState: DragDropState,
    pieces: List<PuzzlePiece>
) {
    if (!dragState.isDragging || dragState.draggedPieceId == null) return
    val pieceId = dragState.draggedPieceId ?: return
    val piece = pieces.firstOrNull { it.id == pieceId } ?: return
    val pieceBitmap = pieceBitmaps[pieceId]

    Box(
        modifier = Modifier
            .offset { IntOffset(dragState.dragOffset.x.toInt(), dragState.dragOffset.y.toInt()) }
            .size(110.dp)
            .shadow(8.dp, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        if (pieceBitmap != null) {
            Image(
                bitmap = pieceBitmap,
                contentDescription = "拖拽碎片",
                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(10.dp)),
                contentScale = ContentScale.Fit
            )
        } else {
            PieceImageContent(
                piece = piece,
                puzzleBitmap = puzzleBitmap,
                cardSize = 110,
                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(10.dp))
            )
        }
    }
}
