package com.puzzle.game

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.create
import platform.posix.memcpy

@OptIn(ExperimentalForeignApi::class)
actual fun readFileBytes(path: String): ByteArray? {
    return try {
        val data = NSData.create(contentsOfFile = path) ?: return null
        val len = data.length.toInt()
        ByteArray(len).apply {
            usePinned { pinned ->
                memcpy(pinned.addressOf(0), data.bytes, data.length)
            }
        }
    } catch (_: Exception) {
        null
    }
}
