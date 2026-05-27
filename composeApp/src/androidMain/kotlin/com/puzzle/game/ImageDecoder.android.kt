package com.puzzle.game

import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap

actual fun decodeToImageBitmap(bytes: ByteArray): ImageBitmap? {
    return try {
        val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        bmp?.asImageBitmap()
    } catch (e: Exception) {
        null
    }
}
