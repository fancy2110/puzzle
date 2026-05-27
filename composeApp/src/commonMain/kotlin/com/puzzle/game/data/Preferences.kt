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
}
