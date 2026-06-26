package com.puzzle.game.analytics

object Analytics {
    private const val EVENT_PREFIX = "puzzle_"

    fun track(event: AnalyticsEvent, properties: Map<String, Any?> = emptyMap()) {
        PlatformAnalytics.trackEvent(
            name = EVENT_PREFIX + event.id,
            properties = properties.normalized()
        )
    }

    fun screen(screen: AnalyticsScreen, source: String? = null) {
        track(
            AnalyticsEvent.ScreenView,
            mapOf(
                "screen" to screen.id,
                "source" to source
            )
        )
    }

    fun click(target: String, screen: AnalyticsScreen? = null) {
        track(
            AnalyticsEvent.Click,
            mapOf(
                "target" to target,
                "screen" to screen?.id
            )
        )
    }
}

enum class AnalyticsScreen(val id: String) {
    Splash("splash"),
    Menu("menu"),
    ThemePicker("theme_picker"),
    ImageSource("image_source"),
    Settings("settings"),
    Game("game")
}

enum class AnalyticsEvent(val id: String) {
    ScreenView("screen_view"),
    Click("click"),
    ThemeSelect("theme_select"),
    StoryPageSelect("story_page_select"),
    PieceCountChange("piece_count_change"),
    GameStart("game_start"),
    AiGameStart("ai_game_start"),
    AiFallback("ai_fallback"),
    GameReady("game_ready"),
    GameError("game_error"),
    PiecePlace("piece_place"),
    GameComplete("game_complete"),
    GamePause("game_pause"),
    GameResume("game_resume"),
    GameRetry("game_retry"),
    GameQuit("game_quit"),
    CelebrationDismiss("celebration_dismiss")
}

expect object PlatformAnalytics {
    fun trackEvent(name: String, properties: Map<String, String>)
}

private fun Map<String, Any?>.normalized(): Map<String, String> {
    return entries
        .mapNotNull { (key, value) ->
            val normalizedKey = key.trim()
            if (normalizedKey.isBlank() || value == null) return@mapNotNull null
            normalizedKey to value.toString().take(256)
        }
        .toMap()
}
