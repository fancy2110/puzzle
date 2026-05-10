package com.puzzle.game.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.puzzle.game.game.GameDifficulty
import com.puzzle.game.game.GamePhase
import com.puzzle.game.game.GameViewModel

@Composable
fun MenuScreen(viewModel: GameViewModel) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "🧩 趣味拼图",
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "适合小朋友的拼图游戏",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = "选择难度",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(12.dp))

        GameDifficulty.entries.forEach { difficulty ->
            val isSelected = state.difficulty == difficulty
            Button(
                onClick = { viewModel.selectDifficulty(difficulty) },
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                colors = if (isSelected) {
                    ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                } else {
                    ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            ) {
                Text(
                    text = difficulty.label + " （${difficulty.pieceCount}块）",
                    fontSize = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { viewModel.startGame() },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            enabled = state.imageSource != null
        ) {
            Text(
                text = "🎮 开始游戏",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = state.promptText,
            onValueChange = { viewModel.setPrompt(it) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("输入你想要的图案描述（可选）") },
            maxLines = 2
        )
    }
}
