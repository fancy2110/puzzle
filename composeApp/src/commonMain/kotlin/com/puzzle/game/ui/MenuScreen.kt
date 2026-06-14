package com.puzzle.game.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.puzzle.game.data.PuzzlePictureGenerator
import com.puzzle.game.data.ThemeData
import com.puzzle.game.game.GameDifficulty
import com.puzzle.game.game.GameViewModel
import com.puzzle.game.ui.component.CloudButton
import com.puzzle.game.ui.component.CoralButton
import com.puzzle.game.ui.component.Plaque
import com.puzzle.game.ui.component.PuzzleBackground
import com.puzzle.game.ui.component.StoneSurface
import com.puzzle.game.ui.theme.PuzzleColors
import com.puzzle.game.ui.theme.PuzzleDimens

@Composable
fun MenuScreen(
    viewModel: GameViewModel,
    onStartGame: () -> Unit,
    onPickTheme: () -> Unit,
    onPickImage: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val theme = state.selectedTheme ?: viewModel.themes.first()
    val preview = remember(theme.id) { PuzzlePictureGenerator.generate(theme, 800, 600) }

    PuzzleBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(PuzzleDimens.PagePadding),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            HomeTopBar(onOpenSettings = onOpenSettings)

            PuzzlePreviewCard(
                theme = theme,
                difficulty = state.difficulty,
                preview = preview,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )

            DifficultySelector(
                selected = state.difficulty,
                onSelect = viewModel::selectDifficulty
            )

            CoralButton(
                text = "开始游戏",
                onClick = onStartGame,
                modifier = Modifier.fillMaxWidth()
            )

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                CloudButton(
                    text = "换主题",
                    onClick = onPickTheme,
                    modifier = Modifier.weight(1f)
                )
                CloudButton(
                    text = "换图片",
                    onClick = onPickImage,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun HomeTopBar(onOpenSettings: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text("Puzzle", fontSize = 34.sp, fontWeight = FontWeight.Bold, color = PuzzleColors.StoneDark)
            Text("选一张图，拼出完整画面", fontSize = 13.sp, color = PuzzleColors.Muted)
        }
        CloudButton(
            text = "设置",
            onClick = onOpenSettings,
            modifier = Modifier.width(78.dp).height(44.dp)
        )
    }
}

@Composable
private fun PuzzlePreviewCard(
    theme: ThemeData,
    difficulty: GameDifficulty,
    preview: ImageBitmap,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(PuzzleDimens.CardRadius))
    ) {
        StoneSurface(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.fillMaxSize().padding(10.dp)) {
                Image(
                    bitmap = preview,
                    contentDescription = theme.name,
                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(14.dp)),
                    contentScale = ContentScale.Crop
                )
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth(0.82f)
                .align(Alignment.BottomCenter)
                .padding(bottom = 18.dp)
        ) {
            Plaque(modifier = Modifier.fillMaxWidth()) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(theme.name, color = PuzzleColors.StoneDark, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(shape = RoundedCornerShape(50), color = PuzzleColors.Teal) {
                        Text(
                            text = "${difficulty.label.substringBefore(" ")} · ${difficulty.pieceCount}片",
                            modifier = Modifier.padding(horizontal = 18.dp, vertical = 7.dp),
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = theme.description,
                        color = PuzzleColors.Muted,
                        fontSize = 11.sp,
                        maxLines = 1,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun DifficultySelector(
    selected: GameDifficulty,
    onSelect: (GameDifficulty) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "难度",
                fontWeight = FontWeight.Bold,
                color = PuzzleColors.StoneDark,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = selected.label,
                color = PuzzleColors.Muted,
                fontSize = 12.sp
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(PuzzleDimens.ControlRadius))
                .background(PuzzleColors.Cloud)
                .border(1.dp, PuzzleColors.Stone.copy(alpha = 0.65f), RoundedCornerShape(PuzzleDimens.ControlRadius))
                .padding(6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            GameDifficulty.entries.forEach { difficulty ->
                val isSelected = difficulty == selected
                Button(
                    onClick = { onSelect(difficulty) },
                    modifier = Modifier.weight(1f).height(38.dp),
                    shape = RoundedCornerShape(18.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) PuzzleColors.Teal else Color.Transparent,
                        contentColor = if (isSelected) Color.White else PuzzleColors.Muted
                    ),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    Text(
                        text = "${difficulty.pieceCount}片",
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }
    }
}
