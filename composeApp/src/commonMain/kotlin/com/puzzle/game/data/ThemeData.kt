package com.puzzle.game.data

import androidx.compose.ui.graphics.Color

data class ThemeData(
    val id: String,
    val name: String,
    val emoji: String,
    val primary: Color,
    val secondary: Color,
    val accent: Color,
    val prompt: String,
    /** If non-null, load this asset file and use native Rust splitting */
    val assetFile: String? = null
)

object ThemePresets {
    val themes = listOf(
        ThemeData(
            id = "demo",
            name = "真实图片",
            emoji = "🖼️",
            primary = Color(0xFF607D8B),
            secondary = Color(0xFFCFD8DC),
            accent = Color(0xFFFF7043),
            prompt = "demo1.png — Rust native splitter",
            assetFile = "demo1.png"
        ),
        ThemeData(
            id = "cat",
            name = "可爱猫咪",
            emoji = "🐱",
            primary = Color(0xFFFFB74D),
            secondary = Color(0xFFFFCC80),
            accent = Color(0xFFFF8A65),
            prompt = "可爱的小猫咪在草地上玩耍"
        ),
        ThemeData(
            id = "balloon",
            name = "彩虹热气球",
            emoji = "🎈",
            primary = Color(0xFF81D4FA),
            secondary = Color(0xFFB3E5FC),
            accent = Color(0xFFFFF176),
            prompt = "五彩缤纷的热气球在蓝天飞翔"
        ),
        ThemeData(
            id = "ocean",
            name = "海底世界",
            emoji = "🐠",
            primary = Color(0xFF4FC3F7),
            secondary = Color(0xFF80DEEA),
            accent = Color(0xFFFFAB91),
            prompt = "海底世界的彩色小鱼和珊瑚"
        ),
        ThemeData(
            id = "forest",
            name = "森林动物",
            emoji = "🦊",
            primary = Color(0xFFA5D6A7),
            secondary = Color(0xFFC8E6C9),
            accent = Color(0xFFFFCC80),
            prompt = "森林里的小动物们在开派对"
        ),
        ThemeData(
            id = "space",
            name = "太空冒险",
            emoji = "🚀",
            primary = Color(0xFF7E57C2),
            secondary = Color(0xFFB39DDB),
            accent = Color(0xFFFFEB3B),
            prompt = "太空中的火箭和闪亮的星星"
        ),
        ThemeData(
            id = "flower",
            name = "美丽花朵",
            emoji = "🌸",
            primary = Color(0xFFF48FB1),
            secondary = Color(0xFFF8BBD0),
            accent = Color(0xFF80D8FF),
            prompt = "花园里的蝴蝶和美丽的花朵"
        )
    )

    fun getById(id: String): ThemeData = themes.first { it.id == id }
}
