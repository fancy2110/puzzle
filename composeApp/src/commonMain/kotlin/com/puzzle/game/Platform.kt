package com.puzzle.game

import androidx.compose.runtime.Composable

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

@Composable
expect fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit)
