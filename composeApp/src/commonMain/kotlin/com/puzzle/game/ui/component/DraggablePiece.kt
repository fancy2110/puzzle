package com.puzzle.game.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalDensity
import com.puzzle.game.engine.model.PuzzlePiece
import com.puzzle.game.game.DragDropState
import kotlin.math.roundToInt

// ── Piece image renderer ─────────────────────────────────

@Composable
internal fun PieceImageContent(
    piece: PuzzlePiece,
    puzzleBitmap: ImageBitmap?,
    cardSize: Int = 70,
    blockSizePx: Int = 1,
    minBlockDp: Float = 14f,
    modifier: Modifier = Modifier
) {
    if (puzzleBitmap == null || piece.pixels.width <= 0 || piece.pixels.height <= 0) return

    val srcLeft = piece.pixels.left.coerceIn(0, (puzzleBitmap.width - 1).coerceAtLeast(0))
    val srcTop = piece.pixels.top.coerceIn(0, (puzzleBitmap.height - 1).coerceAtLeast(0))
    val srcWidth = piece.pixels.width
        .coerceAtMost(puzzleBitmap.width - srcLeft)
        .coerceAtLeast(1)
    val srcHeight = piece.pixels.height
        .coerceAtMost(puzzleBitmap.height - srcTop)
        .coerceAtLeast(1)

    Canvas(modifier = modifier) {
        val path = piece.toLocalPath(size.width, size.height)
        val drawContent = {
            drawImage(
                image = puzzleBitmap,
                srcOffset = IntOffset(srcLeft, srcTop),
                srcSize = IntSize(srcWidth, srcHeight),
                dstOffset = IntOffset.Zero,
                dstSize = IntSize(
                    size.width.toInt().coerceAtLeast(1),
                    size.height.toInt().coerceAtLeast(1)
                )
            )
        }

        if (path != null) {
            clipPath(path) { drawContent() }
        } else {
            drawContent()
        }
    }
}

@Composable
internal fun PieceShapeOverlay(
    piece: PuzzlePiece,
    fillColor: Color,
    strokeColor: Color,
    strokeWidth: Dp,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val path = piece.toLocalPath(size.width, size.height)
        if (path != null) {
            drawPath(path, fillColor)
            drawPath(path, strokeColor, style = Stroke(width = strokeWidth.toPx()))
        } else {
            drawRect(fillColor)
            drawRect(strokeColor, style = Stroke(width = strokeWidth.toPx()))
        }
    }
}

private fun PuzzlePiece.toLocalPath(width: Float, height: Float): Path? {
    if (outline.size < 3 || pixels.width <= 0 || pixels.height <= 0 || width <= 0f || height <= 0f) {
        return null
    }

    val scaleX = width / pixels.width.toFloat()
    val scaleY = height / pixels.height.toFloat()
    return Path().apply {
        outline.forEachIndexed { index, point ->
            val x = (point.x - pixels.left) * scaleX
            val y = (point.y - pixels.top) * scaleY
            if (index == 0) moveTo(x, y) else lineTo(x, y)
        }
        close()
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
                                            startOffset = pieceWindowPos,
                                            pieceSize = Offset(
                                                pieceIntSize.width.toFloat(),
                                                pieceIntSize.height.toFloat()
                                            ),
                                            touchOffset = localOffset
                                        )
                                    },
                                    onDrag = { change, _ ->
                                        change.consume()
                                        dragState.updateDragPointer(pieceWindowPos + change.position)
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
                    CheckIcon(
                        modifier = Modifier.size(22.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
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
    pieces: List<PuzzlePiece>,
    containerWindowOffset: Offset = Offset.Zero,
    modifier: Modifier = Modifier
) {
    if (!dragState.isDragging || dragState.draggedPieceId == null) return
    val pieceId = dragState.draggedPieceId ?: return
    val piece = pieces.firstOrNull { it.id == pieceId } ?: return
    val pieceBitmap = pieceBitmaps[pieceId]
    val density = LocalDensity.current
    val pieceWidth = with(density) {
        dragState.dragPieceSize.x.takeIf { it > 0f }?.toDp() ?: 110.dp
    }
    val pieceHeight = with(density) {
        dragState.dragPieceSize.y.takeIf { it > 0f }?.toDp() ?: 110.dp
    }
    val localOffset = dragState.dragOffset - containerWindowOffset

    Box(
        modifier = modifier
            .offset { IntOffset(localOffset.x.roundToInt(), localOffset.y.roundToInt()) }
            .size(pieceWidth, pieceHeight),
        contentAlignment = Alignment.Center
    ) {
        if (pieceBitmap != null) {
            Image(
                bitmap = pieceBitmap,
                contentDescription = "拖拽碎片",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        } else {
            PieceImageContent(
                piece = piece,
                puzzleBitmap = puzzleBitmap,
                cardSize = 110,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
