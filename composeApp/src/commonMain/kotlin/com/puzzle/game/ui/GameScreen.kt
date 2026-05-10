package com.puzzle.game.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = { viewModel.goToMenu() }) {
                    Text("← 返回", fontSize = 14.sp)
                }
                Text(
                    text = "已拼: ${filledSet.size}/${state.pieces.size}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary
                )
                TextButton(onClick = { viewModel.resetGame() }) {
                    Text("重置", fontSize = 14.sp)
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            PuzzleBoard(
                pieces = state.pieces,
                puzzleBitmap = state.puzzleBitmap,
                placedPieceIds = filledSet,
                dragState = dragState,
                modifier = Modifier.fillMaxWidth().weight(1f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "拖拽碎片到对应的位置",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(4.dp))

            if (state.wrongDropHint) {
                Text(
                    "❌ 再试试！", fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }

            PieceTray(
                pieces = state.pieces,
                puzzleBitmap = state.puzzleBitmap,
                placedPieceIds = filledSet,
                dragState = dragState,
                onDragEnd = { viewModel.handleDragEnd() },
                modifier = Modifier.fillMaxWidth().height(120.dp)
            )
        }

        FloatingDraggedPiece(
            puzzleBitmap = state.puzzleBitmap,
            dragState = dragState,
            pieces = state.pieces
        )
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
