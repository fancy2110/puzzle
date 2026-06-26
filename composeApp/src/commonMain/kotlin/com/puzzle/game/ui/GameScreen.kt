package com.puzzle.game.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
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
import com.puzzle.game.PlatformBackHandler
import com.puzzle.game.game.GamePhase
import com.puzzle.game.game.GameViewModel
import com.puzzle.game.engine.PuzzleConfig
import com.puzzle.game.ui.component.BackIcon
import com.puzzle.game.ui.component.BookIcon
import com.puzzle.game.ui.component.CelebrationOverlay
import com.puzzle.game.ui.component.CheckIcon
import com.puzzle.game.ui.component.CloudButton
import com.puzzle.game.ui.component.CoralButton
import com.puzzle.game.ui.component.ClockIcon
import com.puzzle.game.ui.component.DiamondIcon
import com.puzzle.game.ui.component.FloatingDraggedPiece
import com.puzzle.game.ui.component.FragmaIconButton
import com.puzzle.game.ui.component.HomeIcon
import com.puzzle.game.ui.component.PieceImageContent
import com.puzzle.game.ui.component.PieceShapeOverlay
import com.puzzle.game.ui.component.PauseIcon
import com.puzzle.game.ui.component.PuzzleBackground
import com.puzzle.game.ui.component.StoneSurface
import com.puzzle.game.ui.component.StoryButton
import com.puzzle.game.ui.component.StoryButtonTone
import com.puzzle.game.ui.component.StoryDialogSurface
import com.puzzle.game.ui.adaptive.AdaptiveContent
import com.puzzle.game.ui.adaptive.AdaptiveLayoutMode
import com.puzzle.game.ui.adaptive.AdaptiveSpec
import com.puzzle.game.ui.adaptive.AdaptiveTrayMode
import com.puzzle.game.ui.theme.FragmaDimens
import com.puzzle.game.ui.theme.PuzzleColors
import com.puzzle.game.ui.theme.PuzzleDimens

@Composable
fun GameScreen(
    viewModel: GameViewModel,
    onGoToMenu: () -> Unit,
    onPlayAgain: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    PlatformBackHandler(enabled = true) {
        when {
            state.isPaused -> viewModel.resume()
            state.phase == GamePhase.PLAYING -> viewModel.pause()
            state.phase == GamePhase.COMPLETED || state.phase == GamePhase.ERROR -> onGoToMenu()
            else -> onGoToMenu()
        }
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

    // Pause dialog must be drawn after the game content so it stays above the puzzle.
    if (state.isPaused) {
        PauseDialog(
            onResume = { viewModel.resume() },
            onQuit = onGoToMenu
        )
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
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {},
        contentAlignment = Alignment.Center
    ) {
        StoryDialogSurface(
            modifier = Modifier
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .width(312.dp)
                .padding(24.dp),
            icon = { PauseIcon(modifier = Modifier.size(34.dp), color = PuzzleColors.CoralDark) },
            title = "确定要退出吗？",
            message = "当前进度将丢失"
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StoryButton(
                    text = "继续",
                    onClick = onResume,
                    modifier = Modifier.weight(1f),
                    tone = StoryButtonTone.Secondary,
                    height = 50.dp
                )
                StoryButton(
                    text = "退出",
                    onClick = onQuit,
                    modifier = Modifier.weight(1f),
                    tone = StoryButtonTone.Danger,
                    height = 50.dp
                )
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
        AdaptiveContent { spec ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(spec.pagePadding),
                contentAlignment = Alignment.TopCenter
            ) {
                when (spec.mode) {
                    AdaptiveLayoutMode.TabletLandscape -> TabletLandscapePlayingLayout(
                        spec = spec,
                        state = state,
                        dragState = dragState,
                        filledSet = filledSet,
                        formattedTime = formattedTime,
                        viewModel = viewModel,
                        onBack = onBack
                    )

                    AdaptiveLayoutMode.TabletPortrait -> TabletPortraitPlayingLayout(
                        spec = spec,
                        state = state,
                        dragState = dragState,
                        filledSet = filledSet,
                        formattedTime = formattedTime,
                        viewModel = viewModel,
                        onBack = onBack
                    )

                    AdaptiveLayoutMode.PhoneLandscape -> PhoneLandscapePlayingLayout(
                        state = state,
                        dragState = dragState,
                        filledSet = filledSet,
                        formattedTime = formattedTime,
                        viewModel = viewModel,
                        onBack = onBack
                    )

                    AdaptiveLayoutMode.PhonePortrait,
                    AdaptiveLayoutMode.Constrained -> PhonePortraitPlayingLayout(
                        state = state,
                        dragState = dragState,
                        filledSet = filledSet,
                        formattedTime = formattedTime,
                        viewModel = viewModel,
                        onBack = onBack
                    )
                }

                FloatingDraggedPiece(
                    puzzleBitmap = state.puzzleBitmap,
                    pieceBitmaps = state.pieceBitmaps,
                    dragState = dragState,
                    pieces = state.pieces
                )
            }
        }
    }
}

@Composable
private fun TabletLandscapePlayingLayout(
    spec: AdaptiveSpec,
    state: com.puzzle.game.game.GameState,
    dragState: com.puzzle.game.game.DragDropState,
    filledSet: Set<String>,
    formattedTime: String,
    viewModel: GameViewModel,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .widthIn(max = spec.contentMaxWidth),
        verticalArrangement = Arrangement.spacedBy(spec.paneGap)
    ) {
        GameTopBar(
            onBack = onBack,
            filledCount = filledSet.size,
            totalCount = state.pieces.size,
            timeText = formattedTime
        )
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(spec.paneGap)
        ) {
            GameBoardArea(
                state = state,
                dragState = dragState,
                viewModel = viewModel,
                modifier = Modifier.weight(1f)
            )
            Column(
                modifier = Modifier
                    .width(spec.sideTrayWidth)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                InteractionHintBar(
                    isSelected = dragState.selectedPieceId != null,
                    showWrongHint = state.wrongDropHint
                )
                AdaptivePieceTray(
                    pieces = state.pieces,
                    puzzleBitmap = state.puzzleBitmap,
                    pieceBitmaps = state.pieceBitmaps,
                    placedPieceIds = filledSet,
                    dragState = dragState,
                    trayMode = AdaptiveTrayMode.SideGrid,
                    onDragEnd = { viewModel.handleDragEnd() },
                    modifier = Modifier.weight(1f)
                )
                BottomHint(dragState, viewModel)
            }
        }
    }
}

@Composable
private fun TabletPortraitPlayingLayout(
    spec: AdaptiveSpec,
    state: com.puzzle.game.game.GameState,
    dragState: com.puzzle.game.game.DragDropState,
    filledSet: Set<String>,
    formattedTime: String,
    viewModel: GameViewModel,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .widthIn(max = spec.contentMaxWidth),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(spec.paneGap)
    ) {
        GameTopBar(
            onBack = onBack,
            filledCount = filledSet.size,
            totalCount = state.pieces.size,
            timeText = formattedTime
        )
        GameBoardArea(
            state = state,
            dragState = dragState,
            viewModel = viewModel,
            modifier = Modifier
                .weight(1f)
                .widthIn(max = spec.boardMaxWidth)
                .fillMaxWidth()
        )
        AdaptivePieceTray(
            pieces = state.pieces,
            puzzleBitmap = state.puzzleBitmap,
            pieceBitmaps = state.pieceBitmaps,
            placedPieceIds = filledSet,
            dragState = dragState,
            trayMode = AdaptiveTrayMode.BottomGrid,
            onDragEnd = { viewModel.handleDragEnd() },
            modifier = Modifier.widthIn(max = spec.trayMaxWidth)
        )
        BottomHint(dragState, viewModel)
    }
}

@Composable
private fun PhoneLandscapePlayingLayout(
    state: com.puzzle.game.game.GameState,
    dragState: com.puzzle.game.game.DragDropState,
    filledSet: Set<String>,
    formattedTime: String,
    viewModel: GameViewModel,
    onBack: () -> Unit
) {
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
            Spacer(modifier = Modifier.height(40.dp))
            InteractionHintBar(
                isSelected = dragState.selectedPieceId != null,
                showWrongHint = state.wrongDropHint
            )
            Spacer(modifier = Modifier.height(4.dp))
            AdaptivePieceTray(
                pieces = state.pieces,
                puzzleBitmap = state.puzzleBitmap,
                pieceBitmaps = state.pieceBitmaps,
                placedPieceIds = filledSet,
                dragState = dragState,
                trayMode = AdaptiveTrayMode.HorizontalStrip,
                onDragEnd = { viewModel.handleDragEnd() }
            )
        }
    }
}

@Composable
private fun PhonePortraitPlayingLayout(
    state: com.puzzle.game.game.GameState,
    dragState: com.puzzle.game.game.DragDropState,
    filledSet: Set<String>,
    formattedTime: String,
    viewModel: GameViewModel,
    onBack: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
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
            viewModel = viewModel,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.height(4.dp))
        AdaptivePieceTray(
            pieces = state.pieces,
            puzzleBitmap = state.puzzleBitmap,
            pieceBitmaps = state.pieceBitmaps,
            placedPieceIds = filledSet,
            dragState = dragState,
            trayMode = AdaptiveTrayMode.HorizontalStrip,
            onDragEnd = { viewModel.handleDragEnd() }
        )
        BottomHint(dragState, viewModel)
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
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FragmaIconButton(
            onClick = onBack,
            modifier = Modifier.size(FragmaDimens.TopControlHeight)
        ) {
            BackIcon(modifier = Modifier.size(25.dp))
        }
        ProgressPlaque(
            filledCount = filledCount,
            totalCount = totalCount,
            modifier = Modifier.weight(1f)
        )
        TimePlaque(timeText = timeText)
    }
}

@Composable
private fun ProgressPlaque(
    filledCount: Int,
    totalCount: Int,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(FragmaDimens.TopControlHeight)
            .shadow(9.dp, RoundedCornerShape(FragmaDimens.ProgressPlaqueRadius))
            .border(1.dp, PuzzleColors.Stone.copy(alpha = 0.74f), RoundedCornerShape(FragmaDimens.ProgressPlaqueRadius)),
        shape = RoundedCornerShape(FragmaDimens.ProgressPlaqueRadius),
        color = PuzzleColors.Cloud.copy(alpha = 0.96f)
    ) {
        Box(contentAlignment = Alignment.Center) {
            PlaqueOrnament(modifier = Modifier.matchParentSize())
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                DiamondIcon(modifier = Modifier.size(10.dp))
                BookIcon(modifier = Modifier.size(24.dp))
                Text(
                    text = "已拼 $filledCount/$totalCount",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = PuzzleColors.StoneDark,
                    textAlign = TextAlign.Center
                )
                DiamondIcon(modifier = Modifier.size(10.dp))
            }
        }
    }
}

@Composable
private fun TimePlaque(timeText: String) {
    Surface(
        modifier = Modifier
            .height(FragmaDimens.TopControlHeight)
            .width(FragmaDimens.TimerPlaqueWidth)
            .shadow(8.dp, RoundedCornerShape(FragmaDimens.TimerPlaqueRadius))
            .border(1.dp, PuzzleColors.Stone.copy(alpha = 0.74f), RoundedCornerShape(FragmaDimens.TimerPlaqueRadius)),
        shape = RoundedCornerShape(FragmaDimens.TimerPlaqueRadius),
        color = PuzzleColors.Cloud.copy(alpha = 0.96f)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ClockIcon(modifier = Modifier.size(22.dp))
                Text(
                    timeText,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = PuzzleColors.StoneDark
                )
            }
        }
    }
}

@Composable
private fun PlaqueOrnament(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val midX = size.width / 2f
        drawLine(
            color = PuzzleColors.Gold.copy(alpha = 0.32f),
            start = Offset(18.dp.toPx(), size.height - 8.dp.toPx()),
            end = Offset(size.width - 18.dp.toPx(), size.height - 8.dp.toPx()),
            strokeWidth = 1.dp.toPx()
        )
        val diamond = Path().apply {
            moveTo(midX, 6.dp.toPx())
            lineTo(midX + 6.dp.toPx(), 13.dp.toPx())
            lineTo(midX, 20.dp.toPx())
            lineTo(midX - 6.dp.toPx(), 13.dp.toPx())
            close()
        }
        drawPath(diamond, PuzzleColors.Gold.copy(alpha = 0.42f))
    }
}

@Composable
private fun InteractionHintBar(
    isSelected: Boolean,
    showWrongHint: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(32.dp),
        contentAlignment = Alignment.Center
    ) {
        when {
            showWrongHint -> Text(
                "再试试",
                fontSize = 13.sp,
                color = PuzzleColors.ErrorSoft,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
            isSelected -> Text(
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
                    .padding(vertical = 6.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun BottomHint(dragState: com.puzzle.game.game.DragDropState, viewModel: GameViewModel) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(36.dp),
        contentAlignment = Alignment.Center
    ) {
        if (dragState.selectedPieceId != null) {
            StoryButton(
                text = "取消选择",
                onClick = { dragState.clearSelection() },
                modifier = Modifier.width(112.dp),
                tone = StoryButtonTone.Secondary,
                height = 32.dp
            )
        } else {
            Text(
                "点击或拖动",
                fontSize = 12.sp,
                color = PuzzleColors.Muted,
                textAlign = TextAlign.Center
            )
        }
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
    val blockSize = PuzzleConfig.PIXEL_BLOCK_SIZE
    val gridCols = state.gridCols.takeIf { it > 0 } ?: ceilDiv(imageWidth, blockSize)
    val gridRows = state.gridRows.takeIf { it > 0 } ?: ceilDiv(imageHeight, blockSize)
    val pieces = state.pieces
    val puzzleBitmap = state.puzzleBitmap
    val placedPieceIds = state.cellFilledBy.values.toSet()
    var boardImageWindowPos by remember { mutableStateOf(Offset.Zero) }
    var boardImageSize by remember { mutableStateOf(IntSize.Zero) }

    LaunchedEffect(
        boardImageWindowPos,
        boardImageSize,
        placedPieceIds,
        pieces,
        imageWidth,
        imageHeight
    ) {
        snapshotFlow {
            DragSample(
                isDragging = dragState.isDragging,
                draggedPieceId = dragState.draggedPieceId,
                dragOffset = dragState.dragOffset,
                dragPieceSize = dragState.dragPieceSize
            )
        }.collect { sample ->
            if (!sample.isDragging || sample.draggedPieceId == null || boardImageSize.width <= 0 || boardImageSize.height <= 0) {
                dragState.updateDropTarget(null)
                return@collect
            }

            val draggedPieceId = sample.draggedPieceId
            if (placedPieceIds.contains(draggedPieceId)) {
                dragState.updateDropTarget(null)
                return@collect
            }

            val targetPiece = pieces.firstOrNull { it.id == draggedPieceId } ?: return@collect
            val targetRect = targetPiece.targetRectInWindow(
                imageWidth = imageWidth,
                imageHeight = imageHeight,
                boardPos = boardImageWindowPos,
                boardSize = boardImageSize
            )
            val dragRect = WindowRect(
                left = sample.dragOffset.x,
                top = sample.dragOffset.y,
                right = sample.dragOffset.x + sample.dragPieceSize.x,
                bottom = sample.dragOffset.y + sample.dragPieceSize.y
            )
            val ratio = dragRect.overlapRatio(targetRect)
            if (ratio >= 0.5f) {
                dragState.updateDropTarget(draggedPieceId, ratio)
            } else {
                dragState.updateDropTarget(null, ratio)
            }
        }
    }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            val framePadding = FragmaDimens.BookCanvasPadding
            val boardW = maxWidth - framePadding * 2
            val boardH = maxHeight - framePadding * 2

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
                    .size(displayW + framePadding * 2, displayH + framePadding * 2)
                    .shadow(
                        elevation = 18.dp,
                        shape = RoundedCornerShape(FragmaDimens.BookCanvasRadius),
                        ambientColor = Color.Black.copy(alpha = 0.08f),
                        spotColor = Color.Black.copy(alpha = 0.14f)
                    )
                    .clip(RoundedCornerShape(FragmaDimens.BookCanvasRadius))
                    .background(PuzzleColors.Cloud)
                    .border(2.dp, PuzzleColors.Stone.copy(alpha = 0.88f), RoundedCornerShape(FragmaDimens.BookCanvasRadius))
            ) {
                BookCanvasDecoration(modifier = Modifier.matchParentSize())
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(displayW, displayH)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.72f))
                        .border(1.dp, PuzzleColors.Stone.copy(alpha = 0.42f), RoundedCornerShape(8.dp))
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
                            alpha = 0.34f
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
                                        PieceImageContent(
                                            piece = piece,
                                            puzzleBitmap = puzzleBitmap,
                                            modifier = Modifier.fillMaxSize()
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
                                ) {
                                    PieceShapeOverlay(
                                        piece = piece,
                                        fillColor = if (isDropTarget) PuzzleColors.Teal.copy(alpha = 0.28f)
                                        else PuzzleColors.Gold.copy(alpha = 0.16f),
                                        strokeColor = if (isDropTarget) PuzzleColors.Teal else PuzzleColors.Gold,
                                        strokeWidth = if (isDropTarget) 2.dp else 1.dp,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }
                        }

                    if (gridRows * gridCols <= 2_500) {
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
                                            color = PuzzleColors.Stone.copy(alpha = 0.20f)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {}
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BookCanvasDecoration(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val margin = 10.dp.toPx()
        drawRect(
            color = PuzzleColors.Stone.copy(alpha = 0.18f),
            topLeft = Offset(0f, h - 18.dp.toPx()),
            size = Size(w, 18.dp.toPx())
        )
        val spine = Path().apply {
            moveTo(margin, margin)
            cubicTo(w * 0.12f, h * 0.18f, w * 0.10f, h * 0.82f, margin, h - margin)
        }
        drawPath(
            path = spine,
            color = PuzzleColors.Stone.copy(alpha = 0.44f),
            style = Stroke(width = 2.dp.toPx())
        )

        drawRoundRect(
            color = Color.White.copy(alpha = 0.30f),
            topLeft = Offset(margin * 1.6f, margin * 1.4f),
            size = Size(w - margin * 3.2f, h - margin * 2.8f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(18.dp.toPx(), 18.dp.toPx()),
            style = Stroke(width = 1.dp.toPx())
        )

        val bookmarkWidth = (w * 0.055f).coerceAtLeast(16.dp.toPx())
        val bookmark = Path().apply {
            moveTo(w - margin - bookmarkWidth, h - margin)
            lineTo(w - margin, h - margin)
            lineTo(w - margin, h * 0.77f)
            lineTo(w - margin - bookmarkWidth / 2f, h * 0.82f)
            lineTo(w - margin - bookmarkWidth, h * 0.77f)
            close()
        }
        drawPath(bookmark, PuzzleColors.TealDark.copy(alpha = 0.68f))

        val corner = 22.dp.toPx()
        val gold = PuzzleColors.Gold.copy(alpha = 0.54f)
        listOf(
            Offset(margin, margin),
            Offset(w - margin - corner, margin),
            Offset(margin, h - margin - corner),
            Offset(w - margin - corner, h - margin - corner)
        ).forEach { topLeft ->
            drawRoundRect(
                color = gold,
                topLeft = topLeft,
                size = Size(corner, corner),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx(), 4.dp.toPx())
            )
        }

        drawCircle(
            color = PuzzleColors.Gold.copy(alpha = 0.14f),
            radius = w * 0.08f,
            center = Offset(w * 0.17f, h * 0.83f)
        )
        drawCircle(
            color = PuzzleColors.Teal.copy(alpha = 0.12f),
            radius = w * 0.06f,
            center = Offset(w * 0.85f, h * 0.18f)
        )
    }
}

// ── Adaptive Piece Tray ─────────────────────────────────

@Composable
private fun AdaptivePieceTray(
    pieces: List<com.puzzle.game.engine.model.PuzzlePiece>,
    puzzleBitmap: androidx.compose.ui.graphics.ImageBitmap?,
    pieceBitmaps: Map<String, androidx.compose.ui.graphics.ImageBitmap> = emptyMap(),
    placedPieceIds: Set<String>,
    dragState: com.puzzle.game.game.DragDropState,
    trayMode: AdaptiveTrayMode,
    onDragEnd: () -> Unit,
    modifier: Modifier = Modifier
) {
    val trayModifier = when (trayMode) {
        AdaptiveTrayMode.HorizontalStrip -> modifier
            .fillMaxWidth()
            .height(FragmaDimens.PieceTrayHeight)
        AdaptiveTrayMode.BottomGrid -> modifier
            .fillMaxWidth()
            .height(220.dp)
        AdaptiveTrayMode.SideGrid -> modifier
            .fillMaxHeight()
    }

    Box(
        modifier = trayModifier
            .shadow(8.dp, RoundedCornerShape(FragmaDimens.PieceTrayRadius))
            .clip(RoundedCornerShape(FragmaDimens.PieceTrayRadius))
            .background(PuzzleColors.Cloud)
            .border(1.dp, PuzzleColors.Stone.copy(alpha = 0.72f), RoundedCornerShape(FragmaDimens.PieceTrayRadius))
    ) {
        ScrollTrayDecoration(modifier = Modifier.matchParentSize())
        when (trayMode) {
            AdaptiveTrayMode.HorizontalStrip -> LazyRow(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(horizontal = 8.dp)
            ) {
                items(pieces, key = { it.id }) { piece ->
                    PieceTrayCard(
                        piece = piece,
                        puzzleBitmap = puzzleBitmap,
                        pieceBitmap = pieceBitmaps[piece.id],
                        isPlaced = placedPieceIds.contains(piece.id),
                        dragState = dragState,
                        onDragEnd = onDragEnd,
                        modifier = Modifier
                            .width(FragmaDimens.PieceCardWidth)
                            .fillMaxHeight()
                    )
                }
            }

            AdaptiveTrayMode.BottomGrid,
            AdaptiveTrayMode.SideGrid -> LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 104.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp)
            ) {
                gridItems(pieces, key = { it.id }) { piece ->
                    PieceTrayCard(
                        piece = piece,
                        puzzleBitmap = puzzleBitmap,
                        pieceBitmap = pieceBitmaps[piece.id],
                        isPlaced = placedPieceIds.contains(piece.id),
                        dragState = dragState,
                        onDragEnd = onDragEnd,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(0.92f)
                    )
                }
            }
        }
    }
}

@Composable
private fun PieceTrayCard(
    piece: com.puzzle.game.engine.model.PuzzlePiece,
    puzzleBitmap: androidx.compose.ui.graphics.ImageBitmap?,
    pieceBitmap: androidx.compose.ui.graphics.ImageBitmap?,
    isPlaced: Boolean,
    dragState: com.puzzle.game.game.DragDropState,
    onDragEnd: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isSelected = dragState.selectedPieceId == piece.id
    var pieceWindowPos by remember { mutableStateOf(Offset.Zero) }
    var pieceIntSize by remember { mutableStateOf(IntSize.Zero) }

    Box(
        modifier = modifier
            .onGloballyPositioned { coords ->
                pieceWindowPos = coords.positionInWindow()
                pieceIntSize = coords.size
            }
            .then(
                if (!isPlaced) {
                    Modifier.pointerInput(piece.id) {
                        var touchAnchor = Offset.Zero
                        detectDragGestures(
                            onDragStart = { localOffset ->
                                touchAnchor = localOffset
                                dragState.startDrag(
                                    pieceId = piece.id,
                                    startOffset = pieceWindowPos,
                                    pieceSize = Offset(
                                        pieceIntSize.width.toFloat(),
                                        pieceIntSize.height.toFloat()
                                    )
                                )
                            },
                            onDrag = { change, _ ->
                                change.consume()
                                dragState.updateDrag(pieceWindowPos + change.position - touchAnchor)
                            },
                            onDragEnd = onDragEnd,
                            onDragCancel = { dragState.cancelDrag() }
                        )
                    }
                } else Modifier
            )
            .clip(RoundedCornerShape(FragmaDimens.PieceCardRadius))
            .border(
                width = 1.dp,
                color = if (isSelected) PuzzleColors.Teal.copy(alpha = 0.88f)
                else PuzzleColors.Stone.copy(alpha = 0.52f),
                shape = RoundedCornerShape(FragmaDimens.PieceCardRadius)
            )
            .background(
                when {
                    isPlaced -> PuzzleColors.Teal.copy(alpha = 0.18f)
                    isSelected -> PuzzleColors.Teal.copy(alpha = 0.16f)
                    else -> Color.White.copy(alpha = 0.64f)
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
            CheckIcon(modifier = Modifier.size(24.dp))
        } else if (pieceBitmap != null) {
            Image(
                bitmap = pieceBitmap,
                contentDescription = "碎片",
                modifier = Modifier.fillMaxSize().padding(6.dp).clip(RoundedCornerShape(10.dp)),
                contentScale = ContentScale.Fit
            )
        } else {
            PieceImageContent(
                piece = piece,
                puzzleBitmap = puzzleBitmap,
                cardSize = 100,
                modifier = Modifier.fillMaxSize().padding(6.dp).clip(RoundedCornerShape(10.dp))
            )
        }
        if (isSelected) {
            Canvas(modifier = Modifier.matchParentSize()) {
                drawRoundRect(
                    color = PuzzleColors.Teal.copy(alpha = 0.24f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(14.dp.toPx(), 14.dp.toPx())
                )
                drawRoundRect(
                    color = PuzzleColors.Teal,
                    style = Stroke(width = 2.dp.toPx()),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(14.dp.toPx(), 14.dp.toPx())
                )
            }
        }
    }
}

@Composable
private fun ScrollTrayDecoration(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val roll = 18.dp.toPx()
        drawRect(
            color = PuzzleColors.Stone.copy(alpha = 0.16f),
            topLeft = Offset(roll, 0f),
            size = Size(size.width - roll * 2, size.height)
        )
        drawRoundRect(
            color = PuzzleColors.Stone.copy(alpha = 0.32f),
            topLeft = Offset(0f, 0f),
            size = Size(roll * 1.35f, size.height),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(14.dp.toPx(), 14.dp.toPx())
        )
        drawRoundRect(
            color = PuzzleColors.Stone.copy(alpha = 0.32f),
            topLeft = Offset(size.width - roll * 1.35f, 0f),
            size = Size(roll * 1.35f, size.height),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(14.dp.toPx(), 14.dp.toPx())
        )
        val ribbon = Path().apply {
            moveTo(18.dp.toPx(), size.height - 28.dp.toPx())
            lineTo(50.dp.toPx(), size.height - 28.dp.toPx())
            lineTo(50.dp.toPx(), size.height)
            lineTo(34.dp.toPx(), size.height - 12.dp.toPx())
            lineTo(18.dp.toPx(), size.height)
            close()
        }
        drawPath(ribbon, PuzzleColors.TealDark.copy(alpha = 0.64f))
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
        AdaptiveContent { spec ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(spec.pagePadding)
                    .widthIn(max = spec.contentMaxWidth),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                FragmaIconButton(
                    onClick = onGoToMenu,
                    modifier = Modifier.size(54.dp)
                ) {
                    HomeIcon(modifier = Modifier.size(25.dp))
                }
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
        AdaptiveContent { spec ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(spec.pagePadding)
                    .widthIn(max = 520.dp),
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

private data class DragSample(
    val isDragging: Boolean,
    val draggedPieceId: String?,
    val dragOffset: Offset,
    val dragPieceSize: Offset
)

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

private fun ceilDiv(value: Int, divisor: Int): Int {
    return ((value + divisor - 1) / divisor).coerceAtLeast(1)
}
