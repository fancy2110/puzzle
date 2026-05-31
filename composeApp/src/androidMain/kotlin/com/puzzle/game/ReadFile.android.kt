package com.puzzle.game

import java.io.File

actual fun readFileBytes(path: String): ByteArray? {
    return try {
        File(path).readBytes()
    } catch (_: Exception) {
        null
    }
}
