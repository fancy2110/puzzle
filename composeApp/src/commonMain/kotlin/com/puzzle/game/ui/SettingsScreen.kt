package com.puzzle.game.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.puzzle.game.analytics.Analytics
import com.puzzle.game.analytics.AnalyticsScreen
import com.puzzle.game.data.Preferences
import com.puzzle.game.i18n.AppLanguage
import com.puzzle.game.i18n.LocalAppStrings
import com.puzzle.game.ui.component.BackIcon
import com.puzzle.game.ui.component.BrandMark
import com.puzzle.game.ui.component.CloudButton
import com.puzzle.game.ui.component.FragmaIconButton
import com.puzzle.game.ui.component.PuzzleBackground
import com.puzzle.game.ui.component.StoneSurface
import com.puzzle.game.ui.component.StoryButton
import com.puzzle.game.ui.component.StoryButtonTone
import com.puzzle.game.ui.adaptive.AdaptiveContent
import com.puzzle.game.ui.theme.PuzzleColors
import com.puzzle.game.ui.theme.FragmaDimens
import com.puzzle.game.ui.theme.PuzzleDimens

@Composable
fun SettingsScreen(
    preferences: Preferences,
    language: AppLanguage,
    soundEnabled: Boolean,
    onSoundEnabledChange: (Boolean) -> Unit,
    onLanguageChange: (AppLanguage) -> Unit,
    onBack: () -> Unit,
    onOpenImageSource: () -> Unit,
    onOpenPrivacyPolicy: () -> Unit
) {
    val strings = LocalAppStrings.current
    var referenceEnabled by remember { mutableStateOf(preferences.isReferenceEnabled()) }

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
            SettingsTopBar(onBack = onBack)

            StoneSurface(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SettingRow(
                        title = strings.sound,
                        subtitle = strings.soundSubtitle,
                        trailing = {
                            SettingsSwitch(
                                checked = soundEnabled,
                                onCheckedChange = {
                                    Analytics.click(
                                        target = "toggle_sound",
                                        screen = AnalyticsScreen.Settings,
                                        properties = mapOf("enabled" to it)
                                    )
                                    onSoundEnabledChange(it)
                                }
                            )
                        }
                    )

                    SettingRow(
                        title = strings.referenceImage,
                        subtitle = strings.referenceImageSubtitle,
                        trailing = {
                            SettingsSwitch(
                                checked = referenceEnabled,
                                onCheckedChange = {
                                    Analytics.click(
                                        target = "toggle_reference",
                                        screen = AnalyticsScreen.Settings,
                                        properties = mapOf("enabled" to it)
                                    )
                                    referenceEnabled = it
                                    preferences.setReferenceEnabled(it)
                                }
                            )
                        }
                    )

                    SettingRow(
                        title = strings.imageSource,
                        subtitle = strings.imageSourceSubtitle,
                        trailing = {
                            StoryButton(
                                text = strings.manage,
                                onClick = onOpenImageSource,
                                tone = StoryButtonTone.Secondary,
                                height = 42.dp,
                                modifier = Modifier.width(82.dp)
                            )
                        }
                    )

                    SettingRow(
                        title = strings.language,
                        subtitle = strings.languageSubtitle,
                        trailing = {
                            LanguageMenu(
                                language = language,
                                onLanguageChange = onLanguageChange
                            )
                        }
                    )

                    SettingRow(
                        title = strings.privacyPolicy,
                        subtitle = strings.privacyPolicySubtitle,
                        trailing = {
                            StoryButton(
                                text = strings.view,
                                onClick = onOpenPrivacyPolicy,
                                tone = StoryButtonTone.Secondary,
                                height = 42.dp,
                                modifier = Modifier.width(82.dp)
                            )
                        }
                    )
                }
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        BorderStroke(1.dp, PuzzleColors.Stone.copy(alpha = 0.36f)),
                        RoundedCornerShape(18.dp)
                    ),
                shape = RoundedCornerShape(18.dp),
                color = PuzzleColors.Cloud.copy(alpha = 0.58f)
            ) {
                Text(
                    text = strings.settingsSaved,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    color = PuzzleColors.Muted,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
            }

            Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun SettingsTopBar(onBack: () -> Unit) {
    val strings = LocalAppStrings.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        FragmaIconButton(
            onClick = onBack,
            size = FragmaDimens.SettingsButtonCompact
        ) {
            BackIcon(modifier = Modifier.size(23.dp))
        }
        BrandMark(size = 48.dp)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = strings.settings,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = PuzzleColors.StoneDark
            )
            Text(
                text = strings.settingsSubtitle,
                color = PuzzleColors.Muted,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun LanguageMenu(
    language: AppLanguage,
    onLanguageChange: (AppLanguage) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        StoryButton(
            text = language.nativeName,
            onClick = { expanded = true },
            tone = StoryButtonTone.Secondary,
            height = 42.dp,
            modifier = Modifier.widthIn(min = 112.dp)
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            AppLanguage.entries.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.nativeName) },
                    onClick = {
                        expanded = false
                        onLanguageChange(option)
                    }
                )
            }
        }
    }
}

@Composable
private fun SettingsSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        colors = SwitchDefaults.colors(
            checkedThumbColor = PuzzleColors.Cloud,
            checkedTrackColor = PuzzleColors.Teal,
            checkedBorderColor = PuzzleColors.TealDark.copy(alpha = 0.28f),
            uncheckedThumbColor = PuzzleColors.Cloud,
            uncheckedTrackColor = PuzzleColors.Stone.copy(alpha = 0.40f),
            uncheckedBorderColor = PuzzleColors.Stone.copy(alpha = 0.72f)
        )
    )
}

@Composable
private fun SettingRow(
    title: String,
    subtitle: String,
    trailing: @Composable () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = PuzzleColors.Cloud.copy(alpha = 0.70f),
        border = BorderStroke(1.dp, PuzzleColors.Stone.copy(alpha = 0.36f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = PuzzleColors.StoneDark
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    subtitle,
                    color = PuzzleColors.Muted,
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )
            }
            Box(
                modifier = Modifier.widthIn(min = 64.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                trailing()
            }
        }
    }
}
