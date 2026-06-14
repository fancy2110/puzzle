package com.puzzle.game.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color

object PuzzleColors {
    val Mist = Color(0xFFF7F2EA)
    val Cloud = Color(0xFFFFF9F0)
    val Stone = Color(0xFFD9CBB6)
    val StoneDark = Color(0xFF7A6750)
    val Muted = Color(0xFF8F806E)
    val Teal = Color(0xFF7CAEA5)
    val TealDark = Color(0xFF356D6A)
    val Coral = Color(0xFFE98166)
    val CoralDark = Color(0xFFB85B44)
    val Gold = Color(0xFFCDA45A)
    val ErrorSoft = Color(0xFFC76A5A)
}

object PuzzleDimens {
    val PagePadding = 18.dp
    val CompactPadding = 10.dp
    val CardRadius = 18.dp
    val ControlRadius = 24.dp
    val SmallRadius = 10.dp
    val IconButton = 46.dp
    val PrimaryButtonHeight = 58.dp
    val SecondaryButtonHeight = 50.dp
    val TrayHeight = 140.dp
}

private val LightColors = lightColorScheme(
    primary = PuzzleColors.TealDark,
    onPrimary = Color.White,
    primaryContainer = PuzzleColors.Teal.copy(alpha = 0.22f),
    onPrimaryContainer = PuzzleColors.StoneDark,
    secondary = PuzzleColors.Coral,
    onSecondary = Color.White,
    secondaryContainer = PuzzleColors.Cloud,
    onSecondaryContainer = PuzzleColors.StoneDark,
    tertiary = PuzzleColors.Gold,
    onTertiary = Color.White,
    tertiaryContainer = PuzzleColors.Gold.copy(alpha = 0.18f),
    onTertiaryContainer = PuzzleColors.StoneDark,
    error = PuzzleColors.ErrorSoft,
    onError = Color.White,
    background = PuzzleColors.Mist,
    onBackground = PuzzleColors.StoneDark,
    surface = PuzzleColors.Cloud,
    onSurface = PuzzleColors.StoneDark,
    surfaceVariant = PuzzleColors.Stone.copy(alpha = 0.34f),
    onSurfaceVariant = PuzzleColors.Muted,
    outline = PuzzleColors.Stone
)

@Composable
fun PuzzleGameTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        content = content
    )
}
