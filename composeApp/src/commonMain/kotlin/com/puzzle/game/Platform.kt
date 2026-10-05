package com.puzzle.game

import androidx.compose.runtime.Composable

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

/**
 * Terminates the app process when the user declines required consent.
 * No-op on iOS, where programmatic termination is prohibited.
 */
expect fun exitApplication()

@Composable
expect fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit)
