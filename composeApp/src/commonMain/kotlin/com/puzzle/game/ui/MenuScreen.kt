package com.puzzle.game.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.puzzle.game.data.PuzzlePictureGenerator
import com.puzzle.game.data.ThemeData
import com.puzzle.game.game.GameDifficulty
import com.puzzle.game.game.GameViewModel
import com.puzzle.game.ui.component.BrandLockup
import com.puzzle.game.ui.component.CloudButton
import com.puzzle.game.ui.component.CoralButton
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
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(PuzzleDimens.PagePadding)
        ) {
            val isCompactHeight = maxHeight < 720.dp
            val verticalGap = if (isCompactHeight) 10.dp else 14.dp

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(verticalGap),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                HomeTopBar(onOpenSettings = onOpenSettings, compact = isCompactHeight)

                PuzzlePreviewCard(
                    theme = theme,
                    difficulty = state.difficulty,
                    preview = preview,
                    compact = isCompactHeight,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = if (isCompactHeight) 260.dp else 320.dp, max = 500.dp)
                        .weight(1f)
                )

                DifficultySelector(
                    selected = state.difficulty,
                    onSelect = viewModel::selectDifficulty
                )

                CoralButton(
                    text = "开始拼图",
                    onClick = onStartGame,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
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
}

@Composable
private fun HomeTopBar(onOpenSettings: () -> Unit, compact: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BrandLockup(
            modifier = Modifier.weight(1f),
            compact = compact
        )
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
    compact: Boolean,
    modifier: Modifier = Modifier
) {
    StoneSurface(modifier = modifier, radius = PuzzleDimens.CardRadius) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(PuzzleColors.Mist)
            ) {
                Image(
                    bitmap = preview,
                    contentDescription = theme.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                Surface(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp),
                    shape = RoundedCornerShape(50),
                    color = PuzzleColors.Cloud.copy(alpha = 0.86f)
                ) {
                    Text(
                        "今日画面",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        color = PuzzleColors.TealDark,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(if (compact) 8.dp else 10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = if (compact) 66.dp else 74.dp)
                    .background(
                        PuzzleColors.Cloud.copy(alpha = 0.72f),
                        RoundedCornerShape(16.dp)
                    )
                    .border(
                        BorderStroke(1.dp, PuzzleColors.Stone.copy(alpha = 0.34f)),
                        RoundedCornerShape(16.dp)
                    )
                    .padding(horizontal = 14.dp, vertical = if (compact) 10.dp else 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        theme.name,
                        color = PuzzleColors.StoneDark,
                        fontSize = if (compact) 18.sp else 21.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = theme.description,
                        color = PuzzleColors.Muted,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = PuzzleColors.Teal,
                    shadowElevation = 2.dp
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 9.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = difficulty.label.substringBefore(" "),
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1
                        )
                        Text(
                            text = "${difficulty.pieceCount}片",
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
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
