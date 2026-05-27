package com.puzzle.game.data

import com.puzzle.game.game.GameDifficulty
import platform.Foundation.NSUserDefaults

actual class Preferences {
    private val defaults = NSUserDefaults.standardUserDefaults

    actual fun getSelectedThemeId(): String? =
        defaults.stringForKey("theme_id")

    actual fun setSelectedThemeId(id: String) {
        defaults.setObject(id, forKey = "theme_id")
    }

    actual fun getDifficulty(): String? =
        defaults.stringForKey("difficulty")

    actual fun setDifficulty(difficulty: GameDifficulty) {
        defaults.setObject(difficulty.name, forKey = "difficulty")
    }

    actual fun getApiKey(): String? =
        defaults.stringForKey("tongyi_api_key")

    actual fun setApiKey(key: String) {
        defaults.setObject(key, forKey = "tongyi_api_key")
    }

    actual fun isFirstLaunch(): Boolean =
        !defaults.boolForKey("first_launch_done")

    actual fun setFirstLaunchDone() {
        defaults.setBool(true, forKey = "first_launch_done")
    }
}
