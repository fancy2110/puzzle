package com.puzzle.game.data

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toPixelMap
import com.puzzle.game.ai.AIImageProvider
import com.puzzle.game.ai.GeneratedImage

class AssetImageProvider(
    val theme: ThemeData,
    private val bitmapSupplier: (ThemeData) -> ImageBitmap
) : AIImageProvider {
    override suspend fun generateImage(prompt: String): GeneratedImage {
        return GeneratedImage(
            id = "asset_${theme.id}",
            prompt = theme.description,
            imageUrl = null,
            localPath = null
        )
    }

    fun generateBitmap(): ImageBitmap = bitmapSupplier(theme)
}
