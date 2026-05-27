package com.puzzle.game

import androidx.compose.ui.graphics.ImageBitmap

/**
 * iOS: UIImage → ComposeImageBitmap conversion requires compose-ui-uikit module
 * which may not be available. Return null to trigger PuzzlePictureGenerator fallback.
 * Native piece splitting still works correctly.
 */
actual fun decodeToImageBitmap(bytes: ByteArray): ImageBitmap? = null
