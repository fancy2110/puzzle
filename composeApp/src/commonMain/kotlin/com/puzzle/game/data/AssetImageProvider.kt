package com.puzzle.game.data

import androidx.compose.ui.graphics.ImageBitmap
import com.puzzle.game.ai.AIImageProvider
import com.puzzle.game.ai.GeneratedImage
import com.puzzle.game.ai.ImageGenerationRequest

class AssetImageProvider(
    val theme: ThemeData,
    private val bitmapSupplier: (ThemeData) -> ImageBitmap
) : AIImageProvider {
    override suspend fun generateImage(request: ImageGenerationRequest): GeneratedImage {
        return GeneratedImage(
            id = "asset_${theme.id}",
            prompt = request.prompt.ifBlank { theme.description },
            imageUrl = null,
            localPath = null
        )
    }

    fun generateBitmap(): ImageBitmap = bitmapSupplier(theme)
}
