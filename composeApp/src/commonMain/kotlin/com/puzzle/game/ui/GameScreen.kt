package com.puzzle.game.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.puzzle.game.engine.model.PuzzlePiece
import com.puzzle.game.game.GamePhase
import com.puzzle.game.game.GameViewModel

@Composable
fun GameScreen(viewModel: GameViewModel) {
    val state by viewModel.state.collectAsState()

    when (state.phase) {
        GamePhase.GENERATING -> GeneratingScreen()
        GamePhase.PLAYING -> PlayingScreen(
            pieces = state.pieces,
            placedPieces = state.placedPieces,
            isComplete = state.isComplete,
            onPlacePiece = { viewModel.placePiece(it) },
            onBackToMenu = { viewModel.goToMenu() },
            onReset = { viewModel.resetGame() }
        )
        GamePhase.COMPLETED -> CompletedScreen(
            pieceCount = state.pieces.size,
            onPlayAgain = { viewModel.resetGame() },
            onBackToMenu = { viewModel.goToMenu() }
        )
        else -> {}
    }
}

@Composable
private fun GeneratingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "正在生成拼图...",
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PlayingScreen(
    pieces: List<PuzzlePiece>,
    placedPieces: Set<String>,
    isComplete: Boolean,
    onPlacePiece: (String) -> Unit,
    onBackToMenu: () -> Unit,
    onReset: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onBackToMenu) {
                Text("← 返回")
            }
            Text(
                text = "已拼好: ${placedPieces.size}/${pieces.size}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            TextButton(onClick = onReset) {
                Text("重置")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "拖拽拼图块到这里",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "拼图碎片",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(4.dp))

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 80.dp),
            modifier = Modifier.fillMaxWidth().height(200.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(pieces, key = { it.id }) { piece ->
                val isPlaced = placedPieces.contains(piece.id)
                PieceCard(
                    piece = piece,
                    isPlaced = isPlaced,
                    onClick = {
                        if (!isPlaced) {
                            onPlacePiece(piece.id)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun PieceCard(
    piece: PuzzlePiece,
    isPlaced: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.size(80.dp),
        shape = RoundedCornerShape(8.dp),
        colors = if (isPlaced) {
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
        } else {
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        },
        elevation = if (isPlaced) CardDefaults.cardElevation(defaultElevation = 0.dp)
        else CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (isPlaced) {
                Text("✓", fontSize = 24.sp, color = MaterialTheme.colorScheme.primary)
            } else {
                val randomColor = remember(piece.id) {
                    val colors = listOf(
                        Color(0xFFEADDFF), Color(0xFFFFD8E4),
                        Color(0xFFD0E6FF), Color(0xFFFFF0C8),
                        Color(0xFFD5F5D0), Color(0xFFFFD9C0),
                        Color(0xFFC8E6FF), Color(0xFFF0D0FF)
                    )
                    colors[piece.id.hashCode().mod(colors.size)]
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(randomColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${piece.id.hashCode().mod(100)}",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun CompletedScreen(
    pieceCount: Int,
    onPlayAgain: () -> Unit,
    onBackToMenu: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "🎉",
                fontSize = 64.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "太棒了！",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "你成功完成了${pieceCount}块拼图！",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = onPlayAgain,
                modifier = Modifier.fillMaxWidth(0.6f).height(48.dp)
            ) {
                Text("再来一局", fontSize = 18.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = onBackToMenu,
                modifier = Modifier.fillMaxWidth(0.6f).height(48.dp)
            ) {
                Text("返回菜单", fontSize = 18.sp)
            }
        }
    }
}
