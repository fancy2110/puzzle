package com.puzzle.game.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.puzzle.game.data.PuzzlePictureGenerator
import com.puzzle.game.data.ThemeData
import com.puzzle.game.game.GameViewModel
import com.puzzle.game.ui.component.CloudButton
import com.puzzle.game.ui.component.CoralButton
import com.puzzle.game.ui.component.PuzzleBackground
import com.puzzle.game.ui.theme.PuzzleColors
import com.puzzle.game.ui.theme.PuzzleDimens

@Composable
fun ThemeScreen(
    viewModel: GameViewModel,
    onBack: () -> Unit,
    onConfirm: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val themes = viewModel.themes

    PuzzleBackground {
        Column(
            modifier = Modifier.fillMaxSize().padding(PuzzleDimens.PagePadding)
        ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onBack) {
                Text("‹", fontSize = 30.sp, color = PuzzleColors.StoneDark)
            }
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "选择画面",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = PuzzleColors.StoneDark
            )
            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.width(60.dp))
        }

        Spacer(modifier = Modifier.height(20.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxWidth().weight(1f),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(themes, key = { it.id }) { theme ->
                val isSelected = state.selectedTheme?.id == theme.id
                ThemePickerCard(
                    theme = theme,
                    isSelected = isSelected,
                    onClick = { viewModel.selectTheme(theme.id) }
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        CoralButton(
            text = "开始这张",
            onClick = onConfirm,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        CloudButton(
            text = "确认选择",
            onClick = onConfirm,
            modifier = Modifier.fillMaxWidth()
        )
        }
    }
}

@Composable
private fun ThemePickerCard(
    theme: ThemeData,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().aspectRatio(0.72f),
        shape = RoundedCornerShape(PuzzleDimens.CardRadius),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                PuzzleColors.Cloud
            else
                PuzzleColors.Cloud.copy(alpha = 0.80f)
        ),
        border = if (isSelected) {
            androidx.compose.foundation.BorderStroke(2.dp, PuzzleColors.Coral)
        } else null,
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 8.dp else 2.dp
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            val preview = remember(theme.id) {
                PuzzlePictureGenerator.generate(theme, 400, 300)
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(theme.primary.copy(alpha = 0.18f))
            ) {
                Image(
                    bitmap = preview,
                    contentDescription = theme.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = theme.name,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = PuzzleColors.StoneDark
            )
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                shape = RoundedCornerShape(50),
                color = if (isSelected) PuzzleColors.Coral else PuzzleColors.Stone.copy(alpha = 0.45f)
            ) {
                Text(
                    text = if (isSelected) "✓ 6片" else "6片",
                    fontSize = 13.sp,
                    color = if (isSelected) Color.White else PuzzleColors.Muted,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 5.dp)
                )
            }
        }
    }
}
