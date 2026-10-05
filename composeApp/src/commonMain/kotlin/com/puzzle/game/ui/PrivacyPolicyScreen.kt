package com.puzzle.game.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.puzzle.game.audio.LocalSfxPlayer
import com.puzzle.game.audio.Sfx
import com.puzzle.game.data.AssetLoader
import com.puzzle.game.i18n.LocalAppLanguage
import com.puzzle.game.i18n.LocalAppStrings
import com.puzzle.game.ui.component.BackIcon
import com.puzzle.game.ui.component.FragmaIconButton
import com.puzzle.game.ui.component.PuzzleBackground
import com.puzzle.game.ui.theme.FragmaDimens
import com.puzzle.game.ui.theme.PuzzleColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Full-screen viewer for the packaged privacy policy. Loads the localized
 * markdown-lite text (falls back to English) from Compose resources.
 */
@Composable
fun PrivacyPolicyScreen(onBack: () -> Unit) {
    val strings = LocalAppStrings.current
    val language = LocalAppLanguage.current
    val sfxPlayer = LocalSfxPlayer.current
    var content by remember(language) { mutableStateOf<String?>(null) }

    LaunchedEffect(language) {
        content = withContext(Dispatchers.Default) {
            val candidates = listOf(
                "privacy/policy-${language.code}.md",
                "privacy/policy-en.md"
            )
            candidates.firstNotNullOfOrNull { path ->
                AssetLoader.readBytes(path)?.decodeToString()
            }
        }
    }

    PuzzleBackground {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FragmaIconButton(
                    onClick = {
                        sfxPlayer.play(Sfx.ButtonClick)
                        onBack()
                    },
                    size = FragmaDimens.SettingsButtonCompact
                ) {
                    BackIcon(modifier = Modifier.size(23.dp))
                }
                Text(
                    text = strings.privacyPolicy,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = PuzzleColors.StoneDark
                )
            }

            val policy = content
            if (policy == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PuzzleColors.Teal)
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 28.dp)
                ) {
                    MarkdownLite(policy)
                }
            }
        }
    }
}

/**
 * Minimal markdown subset renderer: `#`/`##` headings, `-` bullets,
 * blank lines as paragraph spacing, everything else a paragraph.
 */
@Composable
private fun MarkdownLite(text: String) {
    text.lines().forEach { rawLine ->
        val line = rawLine.trim()
        when {
            line.startsWith("# ") -> {
                Text(
                    text = line.removePrefix("# "),
                    fontSize = 22.sp,
                    lineHeight = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = PuzzleColors.StoneDark,
                    modifier = Modifier.padding(top = 14.dp, bottom = 6.dp)
                )
            }

            line.startsWith("## ") -> {
                Text(
                    text = line.removePrefix("## "),
                    fontSize = 16.sp,
                    lineHeight = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = PuzzleColors.TealDark,
                    modifier = Modifier.padding(top = 14.dp, bottom = 4.dp)
                )
            }

            line.startsWith("- ") -> {
                Row(
                    modifier = Modifier.padding(vertical = 3.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "•",
                        fontSize = 13.5.sp,
                        color = PuzzleColors.Teal
                    )
                    Text(
                        text = line.removePrefix("- "),
                        fontSize = 13.5.sp,
                        lineHeight = 20.sp,
                        color = PuzzleColors.StoneDark
                    )
                }
            }

            line.isEmpty() -> Spacer(modifier = Modifier.height(6.dp))

            else -> {
                Text(
                    text = line,
                    fontSize = 13.5.sp,
                    lineHeight = 21.sp,
                    color = PuzzleColors.StoneDark,
                    modifier = Modifier.padding(vertical = 3.dp)
                )
            }
        }
    }
}
