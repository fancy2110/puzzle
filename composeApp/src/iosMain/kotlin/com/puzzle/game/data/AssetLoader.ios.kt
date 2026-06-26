package com.puzzle.game.data

import kotlinx.cinterop.*
import platform.Foundation.*

@OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
actual object AssetLoader {
    actual fun readBytes(path: String): ByteArray? {
        return try {
            val directory = path.substringBeforeLast("/", missingDelimiterValue = "")
            val fileName = path.substringAfterLast("/")
            val name = fileName.substringBeforeLast(".")
            val ext = path.substringAfterLast(".", "")
            val filePath = if (directory.isBlank()) {
                NSBundle.mainBundle.pathForResource(name, ext)
            } else {
                NSBundle.mainBundle.pathForResource(name, ext, directory)
            }
                ?: return null

            val handle = NSFileHandle.fileHandleForReadingAtPath(filePath)
                ?: return null
            val data = handle.readDataToEndOfFile()
            handle.closeFile()
            val len = data.length.toInt()
            ByteArray(len).apply {
                usePinned { pinned ->
                    platform.posix.memcpy(pinned.addressOf(0), data.bytes, data.length)
                }
            }
        } catch (e: Exception) {
            null
        }
    }
}
