package com.puzzle.game.analytics

import com.puzzle.logger.PuzzleLog
import kotlin.random.Random

object Analytics {
    private const val EVENT_PREFIX = "puzzle_"
    private const val SCHEMA_VERSION = "2"
    private var appLaunchTracked = false
    private var appReadyTracked = false
    private var commonProperties: Map<String, Any?> = mapOf(
        "session_id" to newTraceId("session"),
        "schema_version" to SCHEMA_VERSION
    )

    fun setLanguage(language: String) {
        commonProperties = commonProperties + ("language" to language)
    }

    fun appLaunch(language: String) {
        setLanguage(language)
        if (appLaunchTracked) return
        appLaunchTracked = true
        track(AnalyticsEvent.AppLaunch)
    }

    fun appReady(startupMs: Long) {
        if (appReadyTracked) return
        appReadyTracked = true
        track(
            AnalyticsEvent.AppReady,
            mapOf("startup_ms" to startupMs.coerceAtLeast(0))
        )
    }

    fun newTraceId(prefix: String): String {
        val entropy = Random.nextLong().toULong().toString(16).padStart(16, '0')
        return "${prefix.take(16)}_$entropy"
    }

    fun track(event: AnalyticsEvent, properties: Map<String, Any?> = emptyMap()) {
        try {
            PlatformAnalytics.trackEvent(
                name = EVENT_PREFIX + event.id,
                properties = normalizeAnalyticsProperties(properties + commonProperties)
            )
        } catch (error: Exception) {
            PuzzleLog.e("Analytics", "Failed to report ${event.id}", error)
        }
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

    fun screenDuration(screen: AnalyticsScreen, durationMs: Long) {
        track(
            AnalyticsEvent.ScreenDuration,
            mapOf(
                "screen" to screen.id,
                "duration_ms" to durationMs,
                "duration_seconds" to durationMs / 1000
            )
        )
    }

    fun click(
        target: String,
        screen: AnalyticsScreen? = null,
        properties: Map<String, Any?> = emptyMap()
    ) {
        track(
            AnalyticsEvent.Click,
            mapOf(
                "target" to target,
                "screen" to screen?.id
            ) + properties
        )
    }

    fun monitorTiming(
        component: String,
        operation: String,
        durationMs: Long,
        properties: Map<String, Any?> = emptyMap()
    ) {
        track(
            AnalyticsEvent.MonitorTiming,
            mapOf(
                "component" to component,
                "operation" to operation,
                "duration_ms" to durationMs.coerceAtLeast(0)
            ) + properties
        )
    }

    fun monitorError(
        component: String,
        operation: String,
        errorCode: String,
        properties: Map<String, Any?> = emptyMap()
    ) {
        track(
            AnalyticsEvent.MonitorError,
            mapOf(
                "component" to component,
                "operation" to operation,
                "error_code" to errorCode,
                "fatal" to false
            ) + properties
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
    AppLaunch("app_launch"),
    AppReady("app_ready"),
    ScreenView("screen_view"),
    ScreenDuration("screen_duration"),
    Click("click"),
    ThemeSelect("theme_select"),
    StoryPageSelect("story_page_select"),
    PieceCountChange("piece_count_change"),
    GameStart("game_start"),
    AiGameStart("ai_game_start"),
    AiFallback("ai_fallback"),
    GameReady("game_ready"),
    GameProgress("game_progress"),
    GameError("game_error"),
    PiecePlace("piece_place"),
    GameComplete("game_complete"),
    GamePause("game_pause"),
    GameResume("game_resume"),
    GameRetry("game_retry"),
    GameQuit("game_quit"),
    StorySceneComplete("story_scene_complete"),
    StoryComplete("story_complete"),
    CelebrationDismiss("celebration_dismiss"),
    MonitorTiming("monitor_timing"),
    MonitorError("monitor_error")
}

expect object PlatformAnalytics {
    /** Initializes the underlying SDK. Called only after privacy consent. */
    fun initialize()
    fun trackEvent(name: String, properties: Map<String, String>)
}

internal fun normalizeAnalyticsProperties(properties: Map<String, Any?>): Map<String, String> {
    return properties.entries
        .mapNotNull { (key, value) ->
            val normalizedKey = key.trim().take(40)
            if (normalizedKey.isBlank() || value == null) return@mapNotNull null
            normalizedKey to value.toString().take(128)
        }
        .take(24)
        .toMap()
}
