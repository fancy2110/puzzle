package com.puzzle.game.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.puzzle.game.game.GamePhase
import com.puzzle.game.game.GameViewModel
import com.puzzle.game.ui.component.CelebrationOverlay
import com.puzzle.game.ui.component.CloudButton
import com.puzzle.game.ui.component.CoralButton
import com.puzzle.game.ui.component.FloatingDraggedPiece
import com.puzzle.game.ui.component.PieceImageContent
import com.puzzle.game.ui.component.Plaque
import com.puzzle.game.ui.component.PuzzleBackground
import com.puzzle.game.ui.component.StoneSurface
import com.puzzle.game.ui.theme.PuzzleColors
import com.puzzle.game.ui.theme.PuzzleDimens

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
    PuzzleBackground {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            StoneSurface(modifier = Modifier.padding(32.dp)) {
                Column(
                    modifier = Modifier.padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(46.dp),
                        color = PuzzleColors.TealDark,
                        strokeWidth = 4.dp
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                    Text(
                        "正在准备拼图",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = PuzzleColors.StoneDark
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "整理画面，生成碎片",
                        fontSize = 13.sp,
                        color = PuzzleColors.Muted
                    )
                }
            }
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

    PuzzleBackground {
    BoxWithConstraints(modifier = Modifier.fillMaxSize().padding(PuzzleDimens.CompactPadding)) {
        val isLandscape = maxWidth > maxHeight
        if (isLandscape) {
            // Landscape: board left, tray right
            Row(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .weight(0.6f)
                        .padding(start = 4.dp, top = 2.dp, bottom = 2.dp, end = 4.dp)
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
                        .padding(end = 4.dp, top = 2.dp, bottom = 2.dp, start = 4.dp)
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
                        pieceBitmaps = state.pieceBitmaps,
                        placedPieceIds = filledSet,
                        dragState = dragState,
                        onDragEnd = { viewModel.handleDragEnd() }
                    )
                }
            }
        } else {
            // Portrait: board top, tray bottom
            Column(modifier = Modifier.fillMaxSize()) {
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
                    pieceBitmaps = state.pieceBitmaps,
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
            pieceBitmaps = state.pieceBitmaps,
            dragState = dragState,
            pieces = state.pieces
        )
    }
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
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CloudButton(
            text = "‹",
            onClick = onBack,
            modifier = Modifier.width(50.dp).height(44.dp)
        )
        Plaque(modifier = Modifier.weight(1f)) {
            Text(
                text = "已拼 $filledCount/$totalCount",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = PuzzleColors.StoneDark,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
        Plaque {
            Text(timeText, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = PuzzleColors.StoneDark)
        }
    }
}

@Composable
private fun SelectionHint() {
    Text(
        text = "已选中碎片，点击棋盘格子放置",
        fontSize = 12.sp,
        color = PuzzleColors.TealDark,
        fontWeight = FontWeight.Medium,
        modifier = Modifier
            .fillMaxWidth()
            .background(
                PuzzleColors.Teal.copy(alpha = 0.16f),
                RoundedCornerShape(PuzzleDimens.SmallRadius)
            )
            .padding(6.dp),
        textAlign = TextAlign.Center
    )
}

@Composable
private fun WrongHint() {
    Text(
        "再试试", fontSize = 13.sp,
        color = PuzzleColors.ErrorSoft,
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
            Text("取消选择", fontSize = 13.sp, color = PuzzleColors.ErrorSoft)
        }
    } else {
        Text(
            "点击或拖动",
            fontSize = 12.sp,
            color = PuzzleColors.Muted,
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
    var boardImageWindowPos by remember { mutableStateOf(Offset.Zero) }
    var boardImageSize by remember { mutableStateOf(IntSize.Zero) }

    LaunchedEffect(
        dragState.isDragging,
        dragState.draggedPieceId,
        dragState.dragOffset,
        dragState.dragPieceSize,
        boardImageWindowPos,
        boardImageSize,
        placedPieceIds
    ) {
        if (!dragState.isDragging || dragState.draggedPieceId == null || boardImageSize.width <= 0 || boardImageSize.height <= 0) {
            dragState.updateDropTarget(null)
            return@LaunchedEffect
        }

        val draggedPieceId = dragState.draggedPieceId ?: return@LaunchedEffect
        if (placedPieceIds.contains(draggedPieceId)) {
            dragState.updateDropTarget(null)
            return@LaunchedEffect
        }

        val targetPiece = pieces.firstOrNull { it.id == draggedPieceId } ?: return@LaunchedEffect
        val targetRect = targetPiece.targetRectInWindow(
            imageWidth = imageWidth,
            imageHeight = imageHeight,
            boardPos = boardImageWindowPos,
            boardSize = boardImageSize
        )
        val dragRect = WindowRect(
            left = dragState.dragOffset.x,
            top = dragState.dragOffset.y,
            right = dragState.dragOffset.x + dragState.dragPieceSize.x,
            bottom = dragState.dragOffset.y + dragState.dragPieceSize.y
        )
        val ratio = dragRect.overlapRatio(targetRect)
        if (ratio >= 0.5f) {
            dragState.updateDropTarget(draggedPieceId, ratio)
        } else {
            dragState.updateDropTarget(null, ratio)
        }
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .border(2.dp, PuzzleColors.Stone, RoundedCornerShape(12.dp))
            .background(PuzzleColors.Cloud),
        contentAlignment = Alignment.Center
    ) {
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize().padding(10.dp),
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
                modifier = Modifier
                    .size(displayW, displayH)
                    .onGloballyPositioned { coords ->
                        boardImageWindowPos = coords.positionInWindow()
                        boardImageSize = coords.size
                    }
            ) {
                // Ghost image
                if (puzzleBitmap != null) {
                    androidx.compose.foundation.Image(
                        bitmap = puzzleBitmap,
                        contentDescription = "原图",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit,
                        alpha = 0.38f
                    )
                }

                if (puzzleBitmap != null) {
                    pieces
                        .filter { placedPieceIds.contains(it.id) }
                        .forEach { piece ->
                            val pieceBitmap = state.pieceBitmaps[piece.id]
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
                                        PuzzleColors.Gold.copy(alpha = 0.58f),
                                        RoundedCornerShape(4.dp)
                                    )
                            ) {
                                if (pieceBitmap != null) {
                                    androidx.compose.foundation.Image(
                                        bitmap = pieceBitmap,
                                        contentDescription = "已放置碎片",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.FillBounds
                                    )
                                } else {
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
                }

                pieces
                    .filter { !placedPieceIds.contains(it.id) }
                    .forEach { piece ->
                        val pieceX = displayW * (piece.pixels.left.toFloat() / imageWidth.toFloat())
                        val pieceY = displayH * (piece.pixels.top.toFloat() / imageHeight.toFloat())
                        val pieceW = displayW * (piece.pixels.width.toFloat() / imageWidth.toFloat())
                        val pieceH = displayH * (piece.pixels.height.toFloat() / imageHeight.toFloat())
                        val isDropTarget = dragState.dropTargetPieceId == piece.id
                        val isTapTarget = dragState.selectedPieceId == piece.id
                        if (isDropTarget || isTapTarget) {
                            Box(
                                modifier = Modifier
                                    .offset(x = pieceX, y = pieceY)
                                    .size(pieceW, pieceH)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        if (isDropTarget) PuzzleColors.Teal.copy(alpha = 0.28f)
                                        else PuzzleColors.Gold.copy(alpha = 0.16f)
                                    )
                                    .border(
                                        width = if (isDropTarget) 2.dp else 1.dp,
                                        color = if (isDropTarget) PuzzleColors.Teal else PuzzleColors.Gold,
                                        shape = RoundedCornerShape(6.dp)
                                    )
                                    .then(
                                        if (dragState.selectedPieceId != null) {
                                            Modifier.clickable {
                                                val result = dragState.tapTarget(piece.id)
                                                if (result != null) {
                                                    viewModel.tryPlacePiece(result.pieceId, result.targetPieceId)
                                                }
                                            }
                                        } else Modifier
                                    )
                            )
                        }
                    }

                for (row in 0 until gridRows) {
                    for (col in 0 until gridCols) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(1f / gridCols)
                                .fillMaxHeight(1f / gridRows)
                                .offset(
                                    x = displayW * col / gridCols,
                                    y = displayH * row / gridRows
                                )
                                .border(
                                    width = 0.3.dp,
                                    color = PuzzleColors.Stone.copy(alpha = 0.24f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {}
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
    pieceBitmaps: Map<String, androidx.compose.ui.graphics.ImageBitmap> = emptyMap(),
    placedPieceIds: Set<String>,
    dragState: com.puzzle.game.game.DragDropState,
    onDragEnd: () -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .height(PuzzleDimens.TrayHeight)
            .clip(RoundedCornerShape(PuzzleDimens.CardRadius))
            .background(PuzzleColors.Cloud)
            .border(1.dp, PuzzleColors.Stone.copy(alpha = 0.70f), RoundedCornerShape(PuzzleDimens.CardRadius))
            .padding(10.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 2.dp)
    ) {
        items(pieces, key = { it.id }) { piece ->
            val isPlaced = placedPieceIds.contains(piece.id)
            val isBeingDragged = dragState.draggedPieceId == piece.id && dragState.isDragging
            val isSelected = dragState.selectedPieceId == piece.id
            var pieceWindowPos by remember { mutableStateOf(Offset.Zero) }
            var pieceIntSize by remember { mutableStateOf(IntSize.Zero) }

            val pieceBitmap = pieceBitmaps[piece.id]
            val cardAspect = pieceBitmap?.let {
                it.width.toFloat() / it.height.coerceAtLeast(1)
            } ?: (piece.pixels.width.toFloat() / piece.pixels.height.coerceAtLeast(1).toFloat())

            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .aspectRatio(cardAspect)
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
                        color = if (isSelected) PuzzleColors.Teal
                        else PuzzleColors.Stone.copy(alpha = 0.55f),
                        shape = RoundedCornerShape(10.dp)
                    )
                    .background(
                        when {
                            isPlaced -> PuzzleColors.Teal.copy(alpha = 0.18f)
                            isSelected -> PuzzleColors.Teal.copy(alpha = 0.18f)
                            else -> Color.White.copy(alpha = 0.72f)
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
                    Text("✓", fontSize = 20.sp, color = PuzzleColors.TealDark)
                } else if (pieceBitmap != null) {
                    Image(
                        bitmap = pieceBitmap,
                        contentDescription = "碎片",
                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Fit
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
    val completedBitmap = state.puzzleBitmap

    PuzzleBackground {
        Column(
            modifier = Modifier.fillMaxSize().padding(PuzzleDimens.PagePadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                CloudButton("⌂", onGoToMenu, modifier = Modifier.width(54.dp).height(46.dp))
                Text(
                    text = "完成拼图",
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = PuzzleColors.Gold
                )
                Spacer(modifier = Modifier.width(54.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))

            StoneSurface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Box(modifier = Modifier.fillMaxSize().padding(12.dp), contentAlignment = Alignment.Center) {
                    if (completedBitmap != null) {
                        Image(
                            bitmap = completedBitmap,
                            contentDescription = "完成的拼图",
                            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(14.dp)),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text(theme?.name ?: "Puzzle", fontSize = 26.sp, color = PuzzleColors.StoneDark)
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            StoneSurface(modifier = Modifier.fillMaxWidth(), radius = PuzzleDimens.CardRadius) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 18.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatItem(value = formatTime(state.elapsedSeconds), label = "用时")
                    StatItem(value = "${state.pieces.size}片", label = "碎片")
                    StatItem(value = "100%", label = "完成")
                }
            }

            Spacer(modifier = Modifier.height(22.dp))
            CoralButton("再来一局", onPlayAgain, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(12.dp))
            CloudButton("换个主题", onGoToMenu, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun StatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = PuzzleColors.StoneDark)
        Spacer(modifier = Modifier.height(4.dp))
        Text(label, fontSize = 13.sp, color = PuzzleColors.Muted)
    }
}

// ── Error Screen ────────────────────────────────────────

@Composable
private fun ErrorScreen(
    message: String,
    onRetry: () -> Unit,
    onGoToMenu: () -> Unit
) {
    PuzzleBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            StoneSurface(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = message,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center,
                        color = PuzzleColors.Muted
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    CoralButton("重试", onRetry, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(10.dp))
                    CloudButton("返回首页", onGoToMenu, modifier = Modifier.fillMaxWidth())
                }
            }
        }
    }
}

// ── Utilities ───────────────────────────────────────────

private fun formatTime(totalSeconds: Long): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "${minutes}:${seconds.toString().padStart(2, '0')}"
}

private data class WindowRect(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float
) {
    private val width: Float get() = (right - left).coerceAtLeast(0f)
    private val height: Float get() = (bottom - top).coerceAtLeast(0f)
    private val area: Float get() = width * height

    fun overlapRatio(other: WindowRect): Float {
        val overlapLeft = maxOf(left, other.left)
        val overlapTop = maxOf(top, other.top)
        val overlapRight = minOf(right, other.right)
        val overlapBottom = minOf(bottom, other.bottom)
        val overlapArea = (overlapRight - overlapLeft).coerceAtLeast(0f) *
                (overlapBottom - overlapTop).coerceAtLeast(0f)
        val denominator = minOf(area, other.area).coerceAtLeast(1f)
        return overlapArea / denominator
    }
}

private fun com.puzzle.game.engine.model.PuzzlePiece.targetRectInWindow(
    imageWidth: Int,
    imageHeight: Int,
    boardPos: Offset,
    boardSize: IntSize
): WindowRect {
    val left = boardPos.x + boardSize.width * (pixels.left.toFloat() / imageWidth.toFloat())
    val top = boardPos.y + boardSize.height * (pixels.top.toFloat() / imageHeight.toFloat())
    val right = boardPos.x + boardSize.width * (pixels.right.toFloat() / imageWidth.toFloat())
    val bottom = boardPos.y + boardSize.height * (pixels.bottom.toFloat() / imageHeight.toFloat())
    return WindowRect(left, top, right, bottom)
}
