package com.puzzle.game.ui.adaptive

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class WindowSizeClass {
    Compact,
    Medium,
    Expanded
}

enum class AdaptiveLayoutMode {
    PhonePortrait,
    PhoneLandscape,
    TabletPortrait,
    TabletLandscape,
    Constrained
}

enum class AdaptiveTrayMode {
    HorizontalStrip,
    BottomGrid,
    SideGrid
}

data class AdaptiveSpec(
    val widthClass: WindowSizeClass,
    val heightClass: WindowSizeClass,
    val mode: AdaptiveLayoutMode,
    val isLandscape: Boolean,
    val isTabletLike: Boolean,
    val pagePadding: Dp,
    val paneGap: Dp,
    val contentMaxWidth: Dp,
    val boardMaxWidth: Dp,
    val trayMaxWidth: Dp,
    val sideTrayWidth: Dp,
    val trayMode: AdaptiveTrayMode
)

@Composable
fun AdaptiveContent(
    modifier: Modifier = Modifier,
    content: @Composable (AdaptiveSpec) -> Unit
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        content(rememberAdaptiveSpec(maxWidth = maxWidth, maxHeight = maxHeight))
    }
}

fun rememberAdaptiveSpec(maxWidth: Dp, maxHeight: Dp): AdaptiveSpec {
    val widthClass = maxWidth.toWindowSizeClass()
    val heightClass = maxHeight.toWindowSizeClass()
    val isLandscape = maxWidth > maxHeight
    val isTabletLike = widthClass != WindowSizeClass.Compact || maxHeight >= 900.dp
    val isConstrained = maxWidth < 420.dp || maxHeight < 620.dp

    val mode = when {
        isConstrained -> AdaptiveLayoutMode.Constrained
        isTabletLike && isLandscape -> AdaptiveLayoutMode.TabletLandscape
        isTabletLike -> AdaptiveLayoutMode.TabletPortrait
        isLandscape -> AdaptiveLayoutMode.PhoneLandscape
        else -> AdaptiveLayoutMode.PhonePortrait
    }

    val trayMode = when (mode) {
        AdaptiveLayoutMode.TabletLandscape -> AdaptiveTrayMode.SideGrid
        AdaptiveLayoutMode.TabletPortrait -> AdaptiveTrayMode.BottomGrid
        AdaptiveLayoutMode.PhoneLandscape,
        AdaptiveLayoutMode.PhonePortrait,
        AdaptiveLayoutMode.Constrained -> AdaptiveTrayMode.HorizontalStrip
    }

    return AdaptiveSpec(
        widthClass = widthClass,
        heightClass = heightClass,
        mode = mode,
        isLandscape = isLandscape,
        isTabletLike = isTabletLike,
        pagePadding = when (mode) {
            AdaptiveLayoutMode.Constrained -> 10.dp
            AdaptiveLayoutMode.TabletLandscape,
            AdaptiveLayoutMode.TabletPortrait -> 28.dp
            else -> 18.dp
        },
        paneGap = when (mode) {
            AdaptiveLayoutMode.TabletLandscape -> 24.dp
            AdaptiveLayoutMode.TabletPortrait -> 18.dp
            else -> 12.dp
        },
        contentMaxWidth = when (mode) {
            AdaptiveLayoutMode.TabletLandscape -> 1120.dp
            AdaptiveLayoutMode.TabletPortrait -> 720.dp
            AdaptiveLayoutMode.PhoneLandscape -> 820.dp
            else -> 420.dp
        },
        boardMaxWidth = when (mode) {
            AdaptiveLayoutMode.TabletLandscape -> 760.dp
            AdaptiveLayoutMode.TabletPortrait -> 680.dp
            AdaptiveLayoutMode.PhoneLandscape -> 560.dp
            else -> 420.dp
        },
        trayMaxWidth = when (mode) {
            AdaptiveLayoutMode.TabletPortrait -> 680.dp
            AdaptiveLayoutMode.TabletLandscape -> 360.dp
            else -> 420.dp
        },
        sideTrayWidth = when (widthClass) {
            WindowSizeClass.Expanded -> 360.dp
            WindowSizeClass.Medium -> 320.dp
            WindowSizeClass.Compact -> 280.dp
        },
        trayMode = trayMode
    )
}

private fun Dp.toWindowSizeClass(): WindowSizeClass = when {
    this < 600.dp -> WindowSizeClass.Compact
    this < 840.dp -> WindowSizeClass.Medium
    else -> WindowSizeClass.Expanded
}
