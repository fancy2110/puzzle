package com.puzzle.game.data

import com.puzzle.game.analytics.Analytics
import com.puzzle.logger.PuzzleLog
import puzzlegame.composeapp.generated.resources.Res

/**
 * Cross-platform loader for files packaged by Compose Resources.
 */
object AssetLoader {
    private val reportedFailures = mutableSetOf<String>()

    suspend fun readBytes(path: String): ByteArray? {
        val resourcePath = "files/${path.removePrefix("files/")}"
        return try {
            Res.readBytes(resourcePath)
        } catch (error: Exception) {
            PuzzleLog.e("AssetLoader", "Unable to read $resourcePath: ${error.message}")
            if (reportedFailures.add(resourcePath)) {
                Analytics.monitorError(
                    component = "resource",
                    operation = "read_builtin_asset",
                    errorCode = "resource_not_found",
                    properties = mapOf(
                        "resource_type" to if (path.startsWith("stories/")) "story_image" else "builtin_image"
                    )
                )
            }
            null
        }
    }
}
