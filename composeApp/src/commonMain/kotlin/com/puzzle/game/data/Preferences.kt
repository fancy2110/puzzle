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

    fun getLanguageCode(): String?
    fun setLanguageCode(code: String)

    /** Version of the privacy policy the user has consented to; null = no consent yet. */
    fun getPrivacyConsentVersion(): String?
    fun setPrivacyConsentVersion(version: String)

    /**
     * Per-story scene completion progress.
     * Returns storyId -> number of scenes completed sequentially from the first scene.
     */
    fun getStoryProgress(): Map<String, Int>

    /** Persists that [storyId] has [completedSceneCount] finished scenes (keeps the max). */
    fun setStoryProgress(storyId: String, completedSceneCount: Int)
}

/** Serialize story progress as `storyId=count;storyId=count`. Entries with count <= 0 are dropped. */
internal fun encodeStoryProgress(progress: Map<String, Int>): String =
    progress.entries
        .filter { it.value > 0 && !it.key.contains('=') && !it.key.contains(';') }
        .joinToString(separator = ";") { "${it.key}=${it.value}" }

/** Parse story progress produced by [encodeStoryProgress]. Malformed entries are ignored. */
internal fun decodeStoryProgress(raw: String?): Map<String, Int> {
    if (raw.isNullOrEmpty()) return emptyMap()
    val result = mutableMapOf<String, Int>()
    for (entry in raw.split(";")) {
        val parts = entry.split("=", limit = 2)
        if (parts.size != 2) continue
        val storyId = parts[0]
        val count = parts[1].toIntOrNull() ?: continue
        if (storyId.isEmpty() || count <= 0) continue
        result[storyId] = maxOf(result[storyId] ?: 0, count)
    }
    return result
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
