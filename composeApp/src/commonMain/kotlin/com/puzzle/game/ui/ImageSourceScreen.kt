package com.puzzle.game.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.puzzle.game.ui.component.BackIcon
import com.puzzle.game.ui.component.FragmaIconButton
import com.puzzle.game.ui.component.ImageIcon
import com.puzzle.game.ui.component.PuzzleBackground
import com.puzzle.game.ui.component.StoneSurface
import com.puzzle.game.ui.component.StoryButton
import com.puzzle.game.ui.component.StoryButtonTone
import com.puzzle.game.ui.adaptive.AdaptiveContent
import com.puzzle.game.i18n.LocalAppStrings

@Composable
fun ImageSourceScreen(
    onBack: () -> Unit,
    onPickBuiltIn: () -> Unit,
    onUseCurrent: () -> Unit
) {
    val strings = LocalAppStrings.current

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
                        .widthIn(max = 640.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
            SimpleTopBar(title = strings.imageSource, onBack = onBack)

            ImageSourceCard(
                title = strings.builtInImages,
                subtitle = strings.builtInImagesSubtitle,
                action = strings.select,
                onClick = onPickBuiltIn
            )
            ImageSourceCard(
                title = strings.currentTheme,
                subtitle = strings.currentThemeSubtitle,
                action = strings.use,
                onClick = onUseCurrent
            )

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = strings.imageSourceFootnote,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
                lineHeight = 18.sp
            )
                }
            }
        }
    }
}

@Composable
private fun ImageSourceCard(
    title: String,
    subtitle: String,
    action: String,
    onClick: () -> Unit
) {
    StoneSurface(modifier = Modifier.fillMaxWidth(), radius = 18.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ImageIcon(modifier = Modifier.size(30.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    subtitle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )
            }
            StoryButton(
                text = action,
                onClick = onClick,
                tone = StoryButtonTone.Secondary,
                height = 42.dp,
                modifier = Modifier.width(74.dp)
            )
        }
    }
}

@Composable
internal fun SimpleTopBar(
    title: String,
    onBack: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        FragmaIconButton(
            onClick = onBack,
            size = 50.dp
        ) {
            BackIcon(modifier = Modifier.size(23.dp))
        }
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.width(48.dp))
    }
}
