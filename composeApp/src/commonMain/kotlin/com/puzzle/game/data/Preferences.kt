package com.puzzle.game.data

import com.puzzle.game.game.GameDifficulty

/**
 * Simple preferences store using expect/actual pattern.
 * Android: SharedPreferences
 * iOS: NSUserDefaults
 */
expect class Preferences {
    fun getSelectedThemeId(): String?
    fun setSelectedThemeId(id: String)

    fun getDifficulty(): String?
    fun setDifficulty(difficulty: GameDifficulty)

    fun getApiKey(): String?
    fun setApiKey(key: String)

    fun isFirstLaunch(): Boolean
    fun setFirstLaunchDone()

    fun isSoundEnabled(): Boolean
    fun setSoundEnabled(enabled: Boolean)

    fun isReferenceEnabled(): Boolean
    fun setReferenceEnabled(enabled: Boolean)
}

/**
 * Platform-specific factory for Preferences.
 * Android: requires a Context (call PreferencesFactory.init(context) in MainActivity first)
 * iOS: no initialization needed
 */
object PreferencesFactory {
    private var platformContext: Any? = null

    fun init(context: Any) {
        platformContext = context
    }

    fun create(): Preferences {
        @Suppress("UNCHECKED_CAST")
        return createPreferences(platformContext)
    }
}

internal expect fun createPreferences(androidContext: Any?): Preferences
