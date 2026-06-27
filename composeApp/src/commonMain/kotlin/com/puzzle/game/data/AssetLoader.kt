package com.puzzle.game.data

import com.puzzle.logger.PuzzleLog
import puzzlegame.composeapp.generated.resources.Res

/**
 * Cross-platform loader for files packaged by Compose Resources.
 */
object AssetLoader {
    suspend fun readBytes(path: String): ByteArray? {
        val resourcePath = "files/${path.removePrefix("files/")}"
        return try {
            Res.readBytes(resourcePath)
        } catch (error: Exception) {
            PuzzleLog.e("AssetLoader", "Unable to read $resourcePath: ${error.message}")
            null
        }
    }
}
