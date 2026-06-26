package com.puzzle.game.ai

import com.puzzle.game.data.ThemeData
import com.puzzle.game.data.ThemePresets
import kotlinx.serialization.Serializable

@Serializable
data class GeneratedImage(
    val id: String,
    val prompt: String,
    val imageUrl: String? = null,
    val localPath: String? = null
)

@Serializable
data class ImageGenerationRequest(
    val prompt: String,
    val negativePrompt: String = CHILD_SAFE_NEGATIVE_PROMPT,
    val size: String = "1024x1024",
    val batchSize: Int = 1,
    val seed: Int? = null
)

const val CHILD_SAFE_NEGATIVE_PROMPT =
    "恐怖，血腥，暴力，武器，危险动作，成人内容，惊吓表情，阴暗压抑，低清晰度，文字，水印，畸形肢体，怪异面部"

interface AIImageProvider {
    suspend fun generateImage(request: ImageGenerationRequest): GeneratedImage

    suspend fun generateImage(prompt: String): GeneratedImage {
        return generateImage(ImageGenerationRequest(prompt = prompt))
    }
}

/**
 * Mock AI provider that returns a no-op GeneratedImage.
 * In Cycle 1-2 this triggers procedural fallback in GameViewModel.
 * Replace with TongyiImageProvider when an API key is configured.
 */
class MockAIImageProvider : AIImageProvider {
    private val themes = listOf(
        "可爱的小猫咪在草地上玩耍",
        "五彩缤纷的热气球在蓝天飞翔",
        "海底世界的彩色小鱼和珊瑚",
        "森林里的小动物们在开派对",
        "太空中的火箭和闪亮的星星",
        "花园里的蝴蝶和美丽的花朵"
    )

    override suspend fun generateImage(request: ImageGenerationRequest): GeneratedImage {
        val prompt = request.prompt
        val themePrompt = if (prompt.isBlank()) themes.random(kotlin.random.Random) else prompt
        // Generate a procedurally varied image — simulates AI generation
        val id = "mock_ai_${kotlin.random.Random.nextInt(10000, 99999)}"
        return GeneratedImage(
            id = id,
            prompt = themePrompt,
            imageUrl = null,
            localPath = null
        )
    }
}

class AIImageGenerator(private val provider: AIImageProvider = MockAIImageProvider()) {
    suspend fun generate(prompt: String): GeneratedImage {
        return provider.generateImage(prompt)
    }

    suspend fun generate(request: ImageGenerationRequest): GeneratedImage {
        return provider.generateImage(request)
    }

    fun getDefaultThemes(): List<String> {
        return listOf(
            "可爱的小猫咪",
            "彩虹热气球",
            "海底小鱼",
            "快乐小动物",
            "太空冒险",
            "美丽花朵"
        )
    }
}
