package com.puzzle.game.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.puzzle.game.data.ThemeData
import com.puzzle.game.game.GameDifficulty
import com.puzzle.game.game.GameViewModel

@Composable
fun MenuScreen(viewModel: GameViewModel) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
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

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "小朋友，选一个喜欢的图案吧！",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxWidth().weight(1f, fill = false),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(viewModel.themes, key = { it.id }) { theme ->
                val isSelected = state.selectedTheme?.id == theme.id
                ThemeCard(
                    theme = theme,
                    isSelected = isSelected,
                    onClick = { viewModel.selectTheme(theme.id) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "选择难度",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            GameDifficulty.entries.forEach { difficulty ->
                val isSelected = state.difficulty == difficulty
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.selectDifficulty(difficulty) },
                    label = {
                        Text(
                            text = difficulty.label.replace(" ", "\n"),
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center,
                            maxLines = 2
                        )
                    },
                    modifier = Modifier.weight(1f).height(48.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { viewModel.startGame() },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = "🎮 开始游戏",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ThemeCard(
    theme: ThemeData,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().aspectRatio(1f),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) theme.primary.copy(alpha = 0.25f)
            else MaterialTheme.colorScheme.surface
        ),
        border = if (isSelected) {
            CardDefaults.outlinedCardBorder(enabled = true).let {
                androidx.compose.foundation.BorderStroke(3.dp, theme.primary)
            }
        } else null,
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 6.dp else 2.dp
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(theme.primary.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = theme.emoji,
                    fontSize = 28.sp
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = theme.name,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (isSelected) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "✓ 已选",
                    fontSize = 12.sp,
                    color = theme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
