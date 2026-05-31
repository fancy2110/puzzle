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
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.clipRect
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

// ── Scale helpers ────────────────────────────────────────

internal fun pieceImageScale(piece: PuzzlePiece, cardSize: Int): Triple<Float, Float, Float> {
    val w = piece.pixels.width
    val h = piece.pixels.height
    if (w <= 0 || h <= 0) return Triple(1f, 0f, 0f)
    val scaleX = cardSize.toFloat() / w
    val scaleY = cardSize.toFloat() / h
    val scale = minOf(scaleX, scaleY, 8f)
    val offX = -piece.pixels.left.toFloat() * scale
    val offY = -piece.pixels.top.toFloat() * scale
    return Triple(scale, offX, offY)
}

// ── Piece image — bounding box crop + block mask ─────────

/**
 * Renders a puzzle piece as a single image cropped from the source bitmap
 * using the piece's bounding box, with a clip mask that hides pixels
 * belonging to other pieces. Only the piece's own grid blocks are visible.
 *
 * @param blockSizePx  the grid cell size used during splitting (default 64)
 */
@Composable
internal fun PieceImageContent(
    piece: PuzzlePiece,
    puzzleBitmap: ImageBitmap?,
    cardSize: Int = 70,
    blockSizePx: Int = 64,
    modifier: Modifier = Modifier
) {
    if (puzzleBitmap == null || piece.items.isEmpty()) return

    val (scale, offX, offY) = pieceImageScale(piece, cardSize)

    // Precompute clip rects (scaled to card coordinates)
    val clipRects = remember(piece.id, cardSize) {
        val s = minOf(cardSize.toFloat() / piece.pixels.width.coerceAtLeast(1),
                       cardSize.toFloat() / piece.pixels.height.coerceAtLeast(1), 8f)
        val originX = -piece.pixels.left.toFloat() * s
        val originY = -piece.pixels.top.toFloat() * s
        piece.items.map { (y, x) ->
            Rect(
                left = originX + x * blockSizePx * s,
                top = originY + y * blockSizePx * s,
                right = originX + (x + 1) * blockSizePx * s,
                bottom = originY + (y + 1) * blockSizePx * s
            )
        }
    }

    Image(
        bitmap = puzzleBitmap,
        contentDescription = "碎片",
        modifier = modifier
            .drawWithContent {
                // Intersect with each block's rect — only draw within them
                for (rect in clipRects) {
                    clipRect(left = rect.left, top = rect.top, right = rect.right, bottom = rect.bottom) {
                        this@drawWithContent.drawContent()
                    }
                }
            }
            .graphicsLayer(
                scaleX = scale, scaleY = scale,
                translationX = offX, translationY = offY
            ),
        contentScale = ContentScale.None
    )
}

// ── PieceBlockContent (legacy, kept for compat) ──────────

@Composable
internal fun PieceBlockContent(
    piece: PuzzlePiece,
    puzzleBitmap: ImageBitmap?,
    blockSizePx: Int = 64,
    blockDp: Int = 14,
    modifier: Modifier = Modifier
) {
    if (puzzleBitmap == null || piece.items.isEmpty()) return

    val blocks = piece.items
    val minY = blocks.minOf { it.y }
    val minX = blocks.minOf { it.x }

    Box(modifier = modifier) {
        for ((y, x) in blocks) {
            val relY = y - minY
            val relX = x - minX
            val srcLeft = x * blockSizePx
            val srcTop = y * blockSizePx

            Image(
                bitmap = puzzleBitmap,
                contentDescription = null,
                modifier = Modifier
                    .offset(x = (relX * blockDp).dp, y = (relY * blockDp).dp)
                    .size(blockDp.dp)
                    .graphicsLayer {
                        val s = blockDp.toFloat() / blockSizePx.toFloat()
                        scaleX = s; scaleY = s
                        translationX = -srcLeft.toFloat() * s
                        translationY = -srcTop.toFloat() * s
                    },
                contentScale = ContentScale.None
            )
        }
    }
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
    dragState: DragDropState,
    pieces: List<PuzzlePiece>
) {
    if (!dragState.isDragging || dragState.draggedPieceId == null) return
    val piece = pieces.firstOrNull { it.id == dragState.draggedPieceId } ?: return

    Box(
        modifier = Modifier
            .offset { IntOffset(dragState.dragOffset.x.toInt(), dragState.dragOffset.y.toInt()) }
            .size(110.dp)
            .shadow(8.dp, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        PieceImageContent(
            piece = piece,
            puzzleBitmap = puzzleBitmap,
            cardSize = 110,
            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(10.dp))
        )
    }
}
