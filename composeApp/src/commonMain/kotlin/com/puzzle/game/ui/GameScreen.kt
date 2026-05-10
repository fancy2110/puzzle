package com.puzzle.game.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.puzzle.game.game.GamePhase
import com.puzzle.game.game.GameViewModel
import com.puzzle.game.ui.component.CelebrationOverlay
import com.puzzle.game.ui.component.FloatingDraggedPiece
import com.puzzle.game.ui.component.PieceTray
import com.puzzle.game.ui.component.PuzzleBoard

@Composable
fun GameScreen(viewModel: GameViewModel) {
    val state by viewModel.state.collectAsState()

    when (state.phase) {
        GamePhase.GENERATING -> GeneratingScreen()
        GamePhase.PLAYING -> PlayingScreen(viewModel)
        GamePhase.COMPLETED -> CompletedScreen(viewModel)
        else -> {}
    }

    if (state.showCelebration) {
        CelebrationOverlay(
            pieceCount = state.pieces.size,
            onDismiss = { viewModel.dismissCelebration() },
            onPlayAgain = { viewModel.resetGame() },
            onBackToMenu = { viewModel.goToMenu() }
        )
    }
}

@Composable
private fun GeneratingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(modifier = Modifier.size(48.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text("正在准备拼图...", fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun PlayingScreen(viewModel: GameViewModel) {
    val state by viewModel.state.collectAsState()
    val dragState = viewModel.dragDropState

    val filledSet = remember(state.cellFilledBy) {
        state.cellFilledBy.values.toSet()
    }

    val showDebug = remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().padding(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = { viewModel.goToMenu() }) {
                    Text("← 返回", fontSize = 14.sp)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "已拼: ${filledSet.size}/${state.pieces.size}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Row {
                    TextButton(onClick = { showDebug.value = !showDebug.value }) {
                        Text("🔍", fontSize = 12.sp)
                    }
                    TextButton(onClick = { viewModel.resetGame() }) {
                        Text("重置", fontSize = 14.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            if (dragState.selectedPieceId != null) {
                Text(
                    text = "✅ 已选中碎片，点击棋盘格子放置",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.fillMaxWidth().background(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f),
                        RoundedCornerShape(6.dp)
                    ).padding(6.dp),
                    textAlign = TextAlign.Center
                )
            }

            if (state.wrongDropHint) {
                Text(
                    "❌ 再试试！", fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            PuzzleBoard(
                pieces = state.pieces,
                puzzleBitmap = state.puzzleBitmap,
                placedPieceIds = filledSet,
                dragState = dragState,
                onCellTap = { row, col ->
                    if (dragState.selectedPieceId != null) {
                        val result = dragState.tapTarget(row, col)
                        if (result != null) {
                            viewModel.tryPlacePiece(
                                result.pieceId,
                                result.targetRow,
                                result.targetCol
                            )
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().weight(1f)
            )

            Spacer(modifier = Modifier.height(4.dp))

            if (dragState.selectedPieceId != null) {
                TextButton(onClick = { dragState.clearSelection() }) {
                    Text("取消选择", fontSize = 13.sp, color = MaterialTheme.colorScheme.error)
                }
            } else {
                Text(
                    "👆 点击碎片选中，再点击棋盘放置；或拖拽碎片",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            PieceTray(
                pieces = state.pieces,
                puzzleBitmap = state.puzzleBitmap,
                placedPieceIds = filledSet,
                dragState = dragState,
                onDragEnd = { viewModel.handleDragEnd() },
                onTapPiece = { pieceId -> dragState.tapSelect(pieceId) },
                modifier = Modifier.fillMaxWidth().height(170.dp)
            )

            if (showDebug.value) {
                DebugOverlay(state)
            }
        }

        FloatingDraggedPiece(
            puzzleBitmap = state.puzzleBitmap,
            dragState = dragState,
            pieces = state.pieces
        )
    }
}

@Composable
private fun DebugOverlay(state: com.puzzle.game.game.GameState) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 2.dp),
        color = Color.Black.copy(alpha = 0.75f),
        shape = RoundedCornerShape(6.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text("🔍 Debug Info", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Text("Phase: ${state.phase}", color = Color.White, fontSize = 10.sp)
            Text("Pieces: ${state.pieces.size}", color = Color.White, fontSize = 10.sp)
            Text("Grid: ${state.gridCols}×${state.gridRows}", color = Color.White, fontSize = 10.sp)
            Text("Bitmap: ${state.puzzleBitmap?.width}×${state.puzzleBitmap?.height}", color = Color.White, fontSize = 10.sp)
            Text("Filled: ${state.cellFilledBy.size} / ${state.correctPositions.size}", color = Color.White, fontSize = 10.sp)
            Text("Theme: ${state.selectedTheme?.name ?: "none"}", color = Color.White, fontSize = 10.sp)
            Text("Difficulty: ${state.difficulty.label}", color = Color.White, fontSize = 10.sp)
        }
    }
}

@Composable
private fun CompletedScreen(viewModel: GameViewModel) {
    Box(
        modifier = Modifier.fillMaxSize().background(
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
        ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "🎉", fontSize = 64.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "太棒了！", fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "你成功完成了拼图！", fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = { viewModel.resetGame() },
                modifier = Modifier.fillMaxWidth(0.6f).height(48.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("再来一局", fontSize = 18.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = { viewModel.goToMenu() },
                modifier = Modifier.fillMaxWidth(0.6f).height(48.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("返回菜单", fontSize = 18.sp)
            }
        }
    }
}
