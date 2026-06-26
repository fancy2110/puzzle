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
import com.puzzle.game.data.AssetLoader
import com.puzzle.game.data.BuiltinStoryImageSet
import com.puzzle.game.data.PuzzlePictureGenerator
import com.puzzle.game.data.StoryPresets
import com.puzzle.game.decodeToImageBitmap
import com.puzzle.game.game.GameViewModel
import com.puzzle.game.ui.component.BackIcon
import com.puzzle.game.ui.component.CheckIcon
import com.puzzle.game.ui.component.CloudButton
import com.puzzle.game.ui.component.CoralButton
import com.puzzle.game.ui.component.FragmaIconButton
import com.puzzle.game.ui.component.PuzzleBackground
import com.puzzle.game.ui.adaptive.AdaptiveContent
import com.puzzle.game.ui.theme.PuzzleColors
import com.puzzle.game.ui.theme.FragmaDimens
import com.puzzle.game.ui.theme.PuzzleDimens

@Composable
fun ThemeScreen(
    viewModel: GameViewModel,
    onBack: () -> Unit,
    onConfirm: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val stories = viewModel.storySets

    PuzzleBackground {
        AdaptiveContent { spec ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(spec.pagePadding),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = spec.contentMaxWidth)
                ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FragmaIconButton(
                onClick = onBack,
                size = FragmaDimens.SettingsButtonCompact
            ) {
                BackIcon(modifier = Modifier.size(23.dp))
            }
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "故事库",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = PuzzleColors.StoneDark
            )
            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.width(60.dp))
        }

        Spacer(modifier = Modifier.height(20.dp))

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 160.dp),
            modifier = Modifier.fillMaxWidth().weight(1f),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(stories, key = { it.id }) { story ->
                val isSelected = state.selectedStoryId == story.id
                StoryPickerCard(
                    story = story,
                    isSelected = isSelected,
                    onClick = { viewModel.selectStory(story.id) }
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        CoralButton(
            text = "开始这个故事",
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
    }
}

@Composable
private fun StoryPickerCard(
    story: BuiltinStoryImageSet,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val theme = StoryPresets.storyPreviewTheme(story)
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
                loadStoryCoverPreview(story)
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
                text = story.title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = PuzzleColors.StoneDark
            )
            Text(
                text = "${story.origin} · ${story.ageRange}",
                fontSize = 11.sp,
                color = PuzzleColors.Muted,
                maxLines = 1,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                shape = RoundedCornerShape(50),
                color = if (isSelected) PuzzleColors.Coral else PuzzleColors.Stone.copy(alpha = 0.45f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    if (isSelected) {
                        CheckIcon(modifier = Modifier.size(13.dp), color = Color.White)
                    }
                    Text(
                        text = "${story.pages.size}幕",
                        fontSize = 13.sp,
                        color = if (isSelected) Color.White else PuzzleColors.Muted,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

private fun loadStoryCoverPreview(story: BuiltinStoryImageSet): androidx.compose.ui.graphics.ImageBitmap {
    val theme = StoryPresets.storyPreviewTheme(story)
    val assetFile = story.pages.firstOrNull()?.assetFile
    if (assetFile != null) {
        val assetBitmap = AssetLoader.readBytes(assetFile)?.let { decodeToImageBitmap(it) }
        if (assetBitmap != null) return assetBitmap
    }
    return PuzzlePictureGenerator.generate(theme, 400, 300)
}
