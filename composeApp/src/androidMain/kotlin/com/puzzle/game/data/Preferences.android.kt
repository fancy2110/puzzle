package com.puzzle.game.data

import android.content.Context
import android.content.SharedPreferences
import com.puzzle.game.game.GameDifficulty

internal actual fun createPreferences(androidContext: Any?): Preferences {
    val ctx = androidContext as? Context
        ?: throw IllegalStateException("PreferencesFactory.init(context) must be called before create()")
    return Preferences(ctx)
}

actual class Preferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("puzzle_prefs", Context.MODE_PRIVATE)

    actual fun getSelectedThemeId(): String? =
        prefs.getString("theme_id", null)

    actual fun setSelectedThemeId(id: String) {
        prefs.edit().putString("theme_id", id).apply()
    }

    actual fun getDifficulty(): String? =
        prefs.getString("difficulty", null)

    actual fun setDifficulty(difficulty: GameDifficulty) {
        prefs.edit().putString("difficulty", difficulty.name).apply()
    }

    actual fun getApiKey(): String? =
        prefs.getString("tongyi_api_key", null)

    actual fun setApiKey(key: String) {
        prefs.edit().putString("tongyi_api_key", key).apply()
    }

    actual fun isFirstLaunch(): Boolean =
        prefs.getBoolean("first_launch", true)

    actual fun setFirstLaunchDone() {
        prefs.edit().putBoolean("first_launch", false).apply()
    }

    actual fun isSoundEnabled(): Boolean =
        prefs.getBoolean("sound_enabled", true)

    actual fun setSoundEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("sound_enabled", enabled).apply()
    }

    actual fun isReferenceEnabled(): Boolean =
        prefs.getBoolean("reference_enabled", true)

    actual fun setReferenceEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("reference_enabled", enabled).apply()
    }
}
