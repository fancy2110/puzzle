package com.puzzle.game

import androidx.compose.ui.graphics.ImageBitmap

/**
 * Cross-platform helper to decode PNG/JPEG bytes into Compose ImageBitmap.
 */
expect fun decodeToImageBitmap(bytes: ByteArray): ImageBitmap?
