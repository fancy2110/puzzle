package com.puzzle.game.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.puzzle.game.PlatformBackHandler
import com.puzzle.game.audio.LocalSfxPlayer
import com.puzzle.game.audio.Sfx
import com.puzzle.game.getPlatform
import com.puzzle.game.i18n.LocalAppStrings
import com.puzzle.game.ui.component.BrandMark
import com.puzzle.game.ui.component.PuzzleBackground
import com.puzzle.game.ui.component.StoneSurface
import com.puzzle.game.ui.component.StoryButton
import com.puzzle.game.ui.component.StoryButtonTone
import com.puzzle.game.ui.theme.PuzzleColors

/**
 * Blocking first-launch guardian-consent gate. Shown before any analytics SDK
 * is initialized; Android system back is intercepted so it cannot be bypassed.
 */
@Composable
fun ConsentGate(
    onAgree: () -> Unit,
    onQuit: () -> Unit,
    onViewPolicy: () -> Unit
) {
    val strings = LocalAppStrings.current
    val sfxPlayer = LocalSfxPlayer.current
    var showDeclineNotice by remember { mutableStateOf(false) }
    val isIos = remember { getPlatform().name.startsWith("iOS", ignoreCase = true) }

    // Never let the gate be dismissed via system back.
    PlatformBackHandler(enabled = true) { }

    PuzzleBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            BrandMark(size = 88.dp)
            Text(
                text = strings.consentTitle,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = PuzzleColors.StoneDark,
                textAlign = TextAlign.Center
            )

            StoneSurface(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = strings.consentMessage,
                        fontSize = 14.sp,
                        lineHeight = 21.sp,
                        color = PuzzleColors.StoneDark
                    )
                    StoryButton(
                        text = strings.consentViewPolicy,
                        onClick = {
                            sfxPlayer.play(Sfx.ButtonClick)
                            onViewPolicy()
                        },
                        tone = StoryButtonTone.Secondary,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            if (!showDeclineNotice) {
                StoryButton(
                    text = strings.consentAgree,
                    onClick = {
                        sfxPlayer.play(Sfx.ButtonClick)
                        onAgree()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                )
                Text(
                    text = strings.consentDisagree,
                    fontSize = 14.sp,
                    color = PuzzleColors.Muted,
                    modifier = Modifier
                        .clickable {
                            sfxPlayer.play(Sfx.ButtonClick)
                            showDeclineNotice = true
                        }
                        .padding(8.dp)
                )
            } else {
                StoneSurface(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = strings.consentDeclineTitle,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = PuzzleColors.CoralDark
                        )
                        Text(
                            text = strings.consentDeclineMessage,
                            fontSize = 14.sp,
                            lineHeight = 21.sp,
                            color = PuzzleColors.StoneDark
                        )
                        if (isIos) {
                            // Programmatic termination is prohibited on iOS;
                            // the only way forward is to return and consent.
                            StoryButton(
                                text = strings.consentDeclineBack,
                                onClick = {
                                    sfxPlayer.play(Sfx.ButtonClick)
                                    showDeclineNotice = false
                                },
                                tone = StoryButtonTone.Secondary,
                                modifier = Modifier.fillMaxWidth()
                            )
                        } else {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                StoryButton(
                                    text = strings.consentDeclineBack,
                                    onClick = {
                                        sfxPlayer.play(Sfx.ButtonClick)
                                        showDeclineNotice = false
                                    },
                                    tone = StoryButtonTone.Secondary,
                                    modifier = Modifier.weight(1f)
                                )
                                StoryButton(
                                    text = strings.consentDeclineQuit,
                                    onClick = {
                                        onQuit()
                                    },
                                    tone = StoryButtonTone.Danger,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
