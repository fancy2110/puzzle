package com.puzzle.game.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.puzzle.game.engine.model.PuzzlePiece
import com.puzzle.game.game.DragDropState

@Composable
fun PuzzleBoard(
    pieces: List<PuzzlePiece>,
    puzzleBitmap: ImageBitmap?,
    placedPieceIds: Set<String>,
    dragState: DragDropState,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val imageWidth = puzzleBitmap?.width ?: 800
    val imageHeight = puzzleBitmap?.height ?: 600
    val blockSize = 64
    val gridCols = (imageWidth / blockSize) + 1
    val gridRows = (imageHeight / blockSize) + 1

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f)),
        contentAlignment = Alignment.Center
    ) {
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            val boardW = maxWidth
            val boardH = maxHeight

            val aspect = imageWidth.toFloat() / imageHeight.toFloat()
            val displayW: Dp
            val displayH: Dp

            if (boardW / boardH < aspect) {
                displayW = boardW
                displayH = boardW / aspect
            } else {
                displayH = boardH
                displayW = boardH * aspect
            }

            Box(
                modifier = Modifier.size(displayW, displayH)
            ) {
                if (puzzleBitmap != null) {
                    Image(
                        bitmap = puzzleBitmap,
                        contentDescription = "拼图参考",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit,
                        alpha = 0.35f
                    )
                }

                for (row in 0 until gridRows) {
                    for (col in 0 until gridCols) {
                        val isFilled = placedPieceIds.any { pid ->
                            val piece = pieces.firstOrNull { it.id == pid }
                            piece?.items?.any { it.y == row && it.x == col } == true
                        }
                        val isDropTarget = dragState.dropTargetCell == Pair(row, col)

                        Box(
                            modifier = Modifier
                                .fillMaxWidth(1f / gridCols)
                                .fillMaxHeight(1f / gridRows)
                                .offset(
                                    x = (displayW * col / gridCols),
                                    y = (displayH * row / gridRows)
                                )
                                .onGloballyPositioned { coords ->
                                    if (dragState.isDragging) {
                                        val offsetInWindow = coords.positionInWindow()
                                        val dragCenterX = dragState.dragOffset.x + dragState.dragPieceSize.x / 2
                                        val dragCenterY = dragState.dragOffset.y + dragState.dragPieceSize.y / 2
                                        if (dragCenterX in offsetInWindow.x..(offsetInWindow.x + coords.size.width) &&
                                            dragCenterY in offsetInWindow.y..(offsetInWindow.y + coords.size.height)
                                        ) {
                                            dragState.dropTargetCell = Pair(row, col)
                                        }
                                    }
                                }
                                .background(
                                    when {
                                        isDropTarget -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                        isFilled -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                                        else -> Color.Transparent
                                    }
                                )
                                .border(
                                    width = if (isDropTarget) 1.dp else 0.3.dp,
                                    color = if (isDropTarget) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isFilled) {
                                Text("✓", fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    textAlign = TextAlign.Center)
                            }
                        }
                    }
                }
            }
        }
    }
}
