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
import com.puzzle.game.data.StoryPresets
import com.puzzle.game.decodeToImageBitmap
import com.puzzle.game.game.GameViewModel
import com.puzzle.game.analytics.Analytics
import com.puzzle.game.analytics.AnalyticsScreen
import com.puzzle.game.i18n.LocalAppStrings
import com.puzzle.game.i18n.LocalAppLanguage
import com.puzzle.game.i18n.StoryLocalization
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun ThemeScreen(
    viewModel: GameViewModel,
    onBack: () -> Unit,
    onConfirm: () -> Unit
) {
    val strings = LocalAppStrings.current
    val state by viewModel.state.collectAsState()
    val progress by viewModel.storyProgress.collectAsState()
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
                text = strings.storyLibrary,
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
                val completedCount = progress[story.id] ?: 0
                StoryPickerCard(
                    story = story,
                    isSelected = isSelected,
                    completedCount = completedCount,
                    onClick = {
                        Analytics.click(
                            target = "select_story",
                            screen = AnalyticsScreen.ThemePicker,
                            properties = mapOf("story_id" to story.id)
                        )
                        viewModel.selectStory(story.id)
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        CoralButton(
            text = strings.startThisStory,
            onClick = onConfirm,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        CloudButton(
            text = strings.confirmSelection,
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
    completedCount: Int,
    onClick: () -> Unit
) {
    val strings = LocalAppStrings.current
    val language = LocalAppLanguage.current
    val localizedStory = remember(story.id, language) {
        StoryLocalization.story(story, language)
    }
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
            val preview by produceState<androidx.compose.ui.graphics.ImageBitmap?>(
                initialValue = null,
                key1 = story.id
            ) {
                value = withContext(Dispatchers.Default) {
                    loadStoryCoverPreview(story)
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(theme.primary.copy(alpha = 0.18f))
            ) {
                if (preview != null) {
                    Image(
                        bitmap = preview!!,
                        contentDescription = localizedStory.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    CircularProgressIndicator(
                        modifier = Modifier.size(28.dp).align(Alignment.Center),
                        color = PuzzleColors.Teal,
                        strokeWidth = 3.dp
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = localizedStory.title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = PuzzleColors.StoneDark
            )
            Text(
                text = "${localizedStory.origin} · ${localizedStory.ageRange}",
                fontSize = 11.sp,
                color = PuzzleColors.Muted,
                maxLines = 1,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            val isStoryComplete = completedCount >= story.pages.size
            val pillColor = when {
                isStoryComplete -> PuzzleColors.Gold
                isSelected -> PuzzleColors.Coral
                else -> PuzzleColors.Stone.copy(alpha = 0.45f)
            }
            val pillText = when {
                completedCount > 0 && !isStoryComplete -> "${completedCount}/${story.pages.size}"
                else -> strings.actLabel(story.pages.size)
            }
            Surface(
                shape = RoundedCornerShape(50),
                color = pillColor
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    if (isSelected || isStoryComplete) {
                        CheckIcon(modifier = Modifier.size(13.dp), color = Color.White)
                    }
                    Text(
                        text = pillText,
                        fontSize = 13.sp,
                        color = if (isSelected || isStoryComplete) Color.White else PuzzleColors.Muted,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

private suspend fun loadStoryCoverPreview(story: BuiltinStoryImageSet): androidx.compose.ui.graphics.ImageBitmap? {
    val assetFile = story.pages.firstOrNull()?.assetFile
    if (assetFile != null) {
        val assetBitmap = AssetLoader.readBytes(assetFile)?.let { decodeToImageBitmap(it) }
        if (assetBitmap != null) return assetBitmap
    }
    return null
}
