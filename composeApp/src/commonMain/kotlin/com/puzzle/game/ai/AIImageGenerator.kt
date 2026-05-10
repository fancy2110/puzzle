package com.puzzle.game.ai

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.random.Random

@Serializable
data class GeneratedImage(
    val id: String,
    val prompt: String,
    val imageUrl: String? = null,
    val localPath: String? = null
)

interface AIImageProvider {
    suspend fun generateImage(prompt: String): GeneratedImage
}

class MockAIImageProvider : AIImageProvider {
    private val themes = listOf(
        "可爱的小猫咪在草地上玩耍",
        "五彩缤纷的热气球在蓝天飞翔",
        "海底世界的彩色小鱼和珊瑚",
        "森林里的小动物们在开派对",
        "太空中的火箭和闪亮的星星",
        "花园里的蝴蝶和美丽的花朵"
    )

    override suspend fun generateImage(prompt: String): GeneratedImage {
        val theme = if (prompt.isBlank()) themes.random(Random) else prompt
        return GeneratedImage(
            id = "img_${Random.nextInt(10000, 99999)}",
            prompt = theme,
            imageUrl = null,
            localPath = null
        )
    }
}

class AIImageGenerator(private val provider: AIImageProvider = MockAIImageProvider()) {
    suspend fun generate(prompt: String): GeneratedImage {
        return provider.generateImage(prompt)
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
