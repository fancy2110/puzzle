package com.puzzle.game

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import org.jetbrains.skia.Image as SkiaImage

/**
 * iOS: decode raw image bytes (PNG/JPEG) into Compose ImageBitmap.
 * Uses Skia Image.makeFromEncoded() which is available on all Compose platforms.
 * Returns null if the bytes cannot be decoded (corrupted or unsupported format).
 */
actual fun decodeToImageBitmap(bytes: ByteArray): ImageBitmap? {
    val skiaImage = try {
        SkiaImage.makeFromEncoded(bytes)
    } catch (_: Exception) {
        null
    }
    return skiaImage?.toComposeImageBitmap()
}
