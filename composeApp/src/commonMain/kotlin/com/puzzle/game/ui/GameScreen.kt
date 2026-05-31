package com.puzzle.game.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.puzzle.game.game.GamePhase
import com.puzzle.game.game.GameViewModel
import com.puzzle.game.ui.component.CelebrationOverlay
import com.puzzle.game.ui.component.FloatingDraggedPiece
import com.puzzle.game.ui.component.PieceImageContent

@Composable
fun GameScreen(
    viewModel: GameViewModel,
    onGoToMenu: () -> Unit,
    onPlayAgain: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    // Handle back press → pause dialog
    if (state.isPaused) {
        PauseDialog(
            onResume = { viewModel.resume() },
            onQuit = onGoToMenu
        )
    }

    when (state.phase) {
        GamePhase.GENERATING -> GeneratingScreen()
        GamePhase.PLAYING -> PlayingScreen(
            viewModel = viewModel,
            onBack = { viewModel.pause() }
        )
        GamePhase.COMPLETED -> CompletedScreen(
            viewModel = viewModel,
            onPlayAgain = onPlayAgain,
            onGoToMenu = onGoToMenu
        )
        GamePhase.ERROR -> ErrorScreen(
            message = state.errorMessage ?: "出了点问题",
            onRetry = { viewModel.retryGame() },
            onGoToMenu = onGoToMenu
        )
        else -> {}
    }

    // Celebration overlay (shown on top of completed screen)
    if (state.showCelebration) {
        CelebrationOverlay(
            pieceCount = state.pieces.size,
            onDismiss = { viewModel.dismissCelebration() },
            onPlayAgain = onPlayAgain,
            onBackToMenu = onGoToMenu
        )
    }
}

// ── Pause Dialog ────────────────────────────────────────

@Composable
private fun PauseDialog(
    onResume: () -> Unit,
    onQuit: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable(enabled = false) { /* block clicks through */ },
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .width(260.dp)
                .padding(24.dp),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "⏸️", fontSize = 40.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "确定要退出吗？",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "当前进度将丢失",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onResume,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("继续游戏", fontSize = 14.sp)
                    }
                    Button(
                        onClick = onQuit,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("退出", fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

// ── Generating Screen ───────────────────────────────────

@Composable
private fun GeneratingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "🎨", fontSize = 48.sp)
            Spacer(modifier = Modifier.height(16.dp))
            CircularProgressIndicator(modifier = Modifier.size(48.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "正在准备拼图...",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ── Playing Screen ──────────────────────────────────────

@Composable
private fun PlayingScreen(
    viewModel: GameViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val dragState = viewModel.dragDropState

    val filledSet = remember(state.cellFilledBy) {
        state.cellFilledBy.values.toSet()
    }

    val formattedTime = formatTime(state.elapsedSeconds)

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isLandscape = maxWidth > maxHeight
        if (isLandscape) {
            // Landscape: board left, tray right
            Row(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .weight(0.6f)
                        .padding(start = 8.dp, top = 6.dp, bottom = 6.dp, end = 4.dp)
                ) {
                    GameTopBar(
                        onBack = onBack,
                        filledCount = filledSet.size,
                        totalCount = state.pieces.size,
                        timeText = formattedTime
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    GameBoardArea(
                        state = state,
                        dragState = dragState,
                        viewModel = viewModel
                    )
                }
                Column(
                    modifier = Modifier
                        .weight(0.4f)
                        .padding(end = 8.dp, top = 6.dp, bottom = 6.dp, start = 4.dp)
                ) {
                    Spacer(modifier = Modifier.height(40.dp)) // space for top bar
                    if (dragState.selectedPieceId != null) {
                        SelectionHint()
                    }
                    if (state.wrongDropHint) {
                        WrongHint()
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    PieceTrayHorizontal(
                        pieces = state.pieces,
                        puzzleBitmap = state.puzzleBitmap,
                        placedPieceIds = filledSet,
                        dragState = dragState,
                        onDragEnd = { viewModel.handleDragEnd() }
                    )
                }
            }
        } else {
            // Portrait: board top, tray bottom
            Column(modifier = Modifier.fillMaxSize().padding(6.dp)) {
                GameTopBar(
                    onBack = onBack,
                    filledCount = filledSet.size,
                    totalCount = state.pieces.size,
                    timeText = formattedTime
                )
                Spacer(modifier = Modifier.height(4.dp))

                if (dragState.selectedPieceId != null) {
                    SelectionHint()
                }
                if (state.wrongDropHint) {
                    WrongHint()
                }

                GameBoardArea(
                    state = state,
                    dragState = dragState,
                    viewModel = viewModel,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.height(4.dp))

                PieceTrayHorizontal(
                    pieces = state.pieces,
                    puzzleBitmap = state.puzzleBitmap,
                    placedPieceIds = filledSet,
                    dragState = dragState,
                    onDragEnd = { viewModel.handleDragEnd() }
                )

                BottomHint(dragState, viewModel)
            }
        }

        // Floating dragged piece overlay
        FloatingDraggedPiece(
            puzzleBitmap = state.puzzleBitmap,
            dragState = dragState,
            pieces = state.pieces
        )
    }
}

// ── Sub-components ──────────────────────────────────────

@Composable
private fun GameTopBar(
    onBack: () -> Unit,
    filledCount: Int,
    totalCount: Int,
    timeText: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextButton(onClick = onBack) {
            Text("← 返回", fontSize = 14.sp)
        }
        Text(
            text = "已拼: $filledCount/$totalCount",
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "⏱ $timeText",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SelectionHint() {
    Text(
        text = "✅ 已选中碎片，点击棋盘格子放置",
        fontSize = 12.sp,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Medium,
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f),
                RoundedCornerShape(6.dp)
            )
            .padding(6.dp),
        textAlign = TextAlign.Center
    )
}

@Composable
private fun WrongHint() {
    Text(
        "❌ 再试试！", fontSize = 13.sp,
        color = MaterialTheme.colorScheme.error,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center
    )
}

@Composable
private fun BottomHint(dragState: com.puzzle.game.game.DragDropState, viewModel: GameViewModel) {
    if (dragState.selectedPieceId != null) {
        TextButton(
            onClick = { dragState.clearSelection() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("取消选择", fontSize = 13.sp, color = MaterialTheme.colorScheme.error)
        }
    } else {
        Text(
            "👆 点击碎片选中，拖拽或点击棋盘放置",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
        )
    }
}

// ── Board Area ──────────────────────────────────────────

@Composable
private fun GameBoardArea(
    state: com.puzzle.game.game.GameState,
    dragState: com.puzzle.game.game.DragDropState,
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val imageWidth = state.puzzleBitmap?.width ?: 800
    val imageHeight = state.puzzleBitmap?.height ?: 600
    val blockSize = 64
    val gridCols = state.gridCols.takeIf { it > 0 } ?: ((imageWidth / blockSize) + 1)
    val gridRows = state.gridRows.takeIf { it > 0 } ?: ((imageHeight / blockSize) + 1)
    val pieces = state.pieces
    val puzzleBitmap = state.puzzleBitmap
    val placedPieceIds = state.cellFilledBy.values.toSet()

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.08f)),
        contentAlignment = Alignment.Center
    ) {
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize().padding(6.dp),
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

            Box(modifier = Modifier.size(displayW, displayH)) {
                // Ghost image
                if (puzzleBitmap != null) {
                    androidx.compose.foundation.Image(
                        bitmap = puzzleBitmap,
                        contentDescription = "原图",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit,
                        alpha = 0.5f
                    )
                }

                if (puzzleBitmap != null) {
                    pieces
                        .filter { placedPieceIds.contains(it.id) }
                        .forEach { piece ->
                            val pieceX = displayW * (piece.pixels.left.toFloat() / imageWidth.toFloat())
                            val pieceY = displayH * (piece.pixels.top.toFloat() / imageHeight.toFloat())
                            val pieceW = displayW * (piece.pixels.width.toFloat() / imageWidth.toFloat())
                            val pieceH = displayH * (piece.pixels.height.toFloat() / imageHeight.toFloat())
                            Box(
                                modifier = Modifier
                                    .offset(x = pieceX, y = pieceY)
                                    .size(pieceW, pieceH)
                                    .clip(RoundedCornerShape(4.dp))
                                    .border(
                                        1.dp,
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.45f),
                                        RoundedCornerShape(4.dp)
                                    )
                            ) {
                                androidx.compose.foundation.Image(
                                    bitmap = puzzleBitmap,
                                    contentDescription = "已放置碎片",
                                    modifier = Modifier
                                        .size(displayW, displayH)
                                        .offset(
                                            x = -pieceX,
                                            y = -pieceY
                                        ),
                                    contentScale = ContentScale.FillBounds
                                )
                            }
                        }
                }

                for (row in 0 until gridRows) {
                    for (col in 0 until gridCols) {
                        val isFilled = state.cellFilledBy.values.any { pid ->
                            val piece = pieces.firstOrNull { it.id == pid }
                            piece?.items?.any { it.y == row && it.x == col } == true
                        }
                        val isDropTarget = dragState.dropTargetCell == Pair(row, col)

                        Box(
                            modifier = Modifier
                                .fillMaxWidth(1f / gridCols)
                                .fillMaxHeight(1f / gridRows)
                                .offset(
                                    x = displayW * col / gridCols,
                                    y = displayH * row / gridRows
                                )
                                .onGloballyPositioned { coords ->
                                    if (dragState.isDragging) {
                                        val pos = coords.positionInWindow()
                                        val cx = dragState.dragOffset.x + dragState.dragPieceSize.x / 2
                                        val cy = dragState.dragOffset.y + dragState.dragPieceSize.y / 2
                                        if (cx in pos.x..(pos.x + coords.size.width) &&
                                            cy in pos.y..(pos.y + coords.size.height)
                                        ) {
                                            dragState.dropTargetCell = Pair(row, col)
                                        }
                                    }
                                }
                                .then(
                                    if (dragState.selectedPieceId != null) {
                                        Modifier.clickable {
                                            val result = dragState.tapTarget(row, col)
                                            if (result != null) {
                                                viewModel.tryPlacePiece(
                                                    result.pieceId,
                                                    result.targetRow,
                                                    result.targetCol
                                                )
                                            }
                                        }
                                    } else Modifier
                                )
                                .background(
                                    when {
                                        isDropTarget -> MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                                        isFilled -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.10f)
                                        else -> Color.Transparent
                                    }
                                )
                                .border(
                                    width = if (isDropTarget) 1.5.dp else 0.3.dp,
                                    color = when {
                                        isDropTarget -> MaterialTheme.colorScheme.primary
                                        isFilled -> MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                                        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            AnimatedContent(
                                targetState = isFilled,
                                transitionSpec = {
                                    fadeIn(animationSpec = tween(300)) togetherWith
                                            fadeOut(animationSpec = tween(200))
                                },
                                label = "cell_fill"
                            ) { filled ->
                                if (filled) Spacer(modifier = Modifier.size(1.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── Horizontal Piece Tray ───────────────────────────────

@Composable
private fun PieceTrayHorizontal(
    pieces: List<com.puzzle.game.engine.model.PuzzlePiece>,
    puzzleBitmap: androidx.compose.ui.graphics.ImageBitmap?,
    placedPieceIds: Set<String>,
    dragState: com.puzzle.game.game.DragDropState,
    onDragEnd: () -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        items(pieces, key = { it.id }) { piece ->
            val isPlaced = placedPieceIds.contains(piece.id)
            val isBeingDragged = dragState.draggedPieceId == piece.id && dragState.isDragging
            val isSelected = dragState.selectedPieceId == piece.id
            var pieceWindowPos by remember { mutableStateOf(Offset.Zero) }
            var pieceIntSize by remember { mutableStateOf(IntSize.Zero) }

            Box(
                modifier = Modifier
                    .size(90.dp)
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
                                        dragState.updateDrag(dragState.dragOffset + dragAmount)
                                    },
                                    onDragEnd = onDragEnd,
                                    onDragCancel = { dragState.cancelDrag() }
                                )
                            }
                        } else Modifier
                    )
                    .clip(RoundedCornerShape(10.dp))
                    .border(
                        width = if (isSelected) 3.dp else 1.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(10.dp)
                    )
                    .background(
                        when {
                            isPlaced -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                            isSelected -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                            else -> MaterialTheme.colorScheme.surface
                        }
                    )
                    .clickable(enabled = !isPlaced) {
                        if (isSelected) {
                            dragState.clearSelection()
                        } else {
                            dragState.tapSelect(piece.id)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                if (isPlaced) {
                    Text("✓", fontSize = 20.sp, color = MaterialTheme.colorScheme.primary)
                } else {
                    PieceImageContent(
                        piece = piece,
                        puzzleBitmap = puzzleBitmap,
                        cardSize = 90,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(8.dp))
                            .then(if (isBeingDragged) Modifier.background(Color.White.copy(alpha = 0.35f)) else Modifier)
                    )
                }
            }
        }
    }
}

// ── Completed Screen ────────────────────────────────────

@Composable
private fun CompletedScreen(
    viewModel: GameViewModel,
    onPlayAgain: () -> Unit,
    onGoToMenu: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val theme = state.selectedTheme

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "🎉", fontSize = 64.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "太棒了！",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "你成功完成了拼图！",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${theme?.emoji ?: "🧩"} ${theme?.name ?: ""} · ⏱ ${formatTime(state.elapsedSeconds)} · 📐 ${state.pieces.size}片",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(28.dp))
            Button(
                onClick = onPlayAgain,
                modifier = Modifier.fillMaxWidth(0.6f).height(48.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("再来一局", fontSize = 18.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = onGoToMenu,
                modifier = Modifier.fillMaxWidth(0.6f).height(48.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("返回菜单", fontSize = 18.sp)
            }
        }
    }
}

// ── Error Screen ────────────────────────────────────────

@Composable
private fun ErrorScreen(
    message: String,
    onRetry: () -> Unit,
    onGoToMenu: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "😵", fontSize = 64.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onRetry,
            modifier = Modifier.fillMaxWidth(0.6f).height(48.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("重试", fontSize = 18.sp)
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(
            onClick = onGoToMenu,
            modifier = Modifier.fillMaxWidth(0.6f).height(48.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("返回菜单", fontSize = 18.sp)
        }
    }
}

// ── Utilities ───────────────────────────────────────────

private fun formatTime(totalSeconds: Long): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "${minutes}:${seconds.toString().padStart(2, '0')}"
}
