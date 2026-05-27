package com.puzzle.game.navigation

/**
 * All screens in the app. Used by NavigationViewModel to manage the screen stack.
 */
sealed class Screen {
    /** Shown briefly at startup, auto-transitions to Menu */
    data object Splash : Screen()

    /** Main menu: start game, choose theme, difficulty */
    data object Menu : Screen()

    /** Theme picker grid */
    data object ThemePicker : Screen()

    /** Pick a source for puzzle images */
    data object ImageSource : Screen()

    /** App settings */
    data object Settings : Screen()

    /** Core game screen — handles GENERATING, PLAYING, COMPLETED internally */
    data object Game : Screen()
}
