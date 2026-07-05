package com.puzzle.game.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.Font
import puzzlegame.composeapp.generated.resources.Res
import puzzlegame.composeapp.generated.resources.app_cjk

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

object FragmaDimens {
    val PageHorizontal = 20.dp
    val PageVertical = 10.dp
    val BrandMark = 62.dp
    val BrandMarkCompact = 50.dp
    val SettingsButton = 58.dp
    val SettingsButtonCompact = 50.dp
    val TopControlHeight = 54.dp
    val ProgressPlaqueRadius = 20.dp
    val TimerPlaqueWidth = 86.dp
    val TimerPlaqueRadius = 18.dp
    val StoryCardRadius = 30.dp
    val StoryImageRadius = 24.dp
    val StoryCardPadding = 14.dp
    val StoryCardPaddingCompact = 10.dp
    val SliderPanelRadius = 26.dp
    val PieceTrayHeight = 150.dp
    val PieceTrayRadius = 22.dp
    val PieceCardWidth = 116.dp
    val PieceCardRadius = 14.dp
    val BookCanvasRadius = 18.dp
    val BookCanvasPadding = 24.dp
    val PrimaryActionHeight = 58.dp
    val SecondaryActionHeight = 52.dp
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
    val appFontFamily = FontFamily(
        Font(Res.font.app_cjk, FontWeight.Normal),
        Font(Res.font.app_cjk, FontWeight.Medium),
        Font(Res.font.app_cjk, FontWeight.SemiBold),
        Font(Res.font.app_cjk, FontWeight.Bold)
    )
    val typography = Typography().withFontFamily(appFontFamily)

    MaterialTheme(
        colorScheme = LightColors,
        typography = typography,
        content = content
    )
}

private fun Typography.withFontFamily(fontFamily: FontFamily): Typography = Typography(
    displayLarge = displayLarge.withFontFamily(fontFamily),
    displayMedium = displayMedium.withFontFamily(fontFamily),
    displaySmall = displaySmall.withFontFamily(fontFamily),
    headlineLarge = headlineLarge.withFontFamily(fontFamily),
    headlineMedium = headlineMedium.withFontFamily(fontFamily),
    headlineSmall = headlineSmall.withFontFamily(fontFamily),
    titleLarge = titleLarge.withFontFamily(fontFamily),
    titleMedium = titleMedium.withFontFamily(fontFamily),
    titleSmall = titleSmall.withFontFamily(fontFamily),
    bodyLarge = bodyLarge.withFontFamily(fontFamily),
    bodyMedium = bodyMedium.withFontFamily(fontFamily),
    bodySmall = bodySmall.withFontFamily(fontFamily),
    labelLarge = labelLarge.withFontFamily(fontFamily),
    labelMedium = labelMedium.withFontFamily(fontFamily),
    labelSmall = labelSmall.withFontFamily(fontFamily)
)

private fun TextStyle.withFontFamily(fontFamily: FontFamily): TextStyle = copy(fontFamily = fontFamily)
