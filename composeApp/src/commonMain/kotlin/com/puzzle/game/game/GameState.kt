package com.puzzle.game.game

import androidx.compose.ui.graphics.ImageBitmap
import com.puzzle.game.data.StoryPresets
import com.puzzle.game.data.ThemeData
import com.puzzle.game.engine.model.PuzzlePiece

enum class GameDifficulty(val pieceCount: Int, val label: String) {
    VERY_EASY(4, "超简单 (2×2)"),
    EASY(6, "简单 (2×3)"),
    MEDIUM(12, "中等 (3×4)"),
    HARD(20, "困难 (4×5)")
}

enum class GamePhase {
    MENU,
    GENERATING,
    PLAYING,
    COMPLETED,
    ERROR
}

data class GameState(
    val phase: GamePhase = GamePhase.MENU,
    val difficulty: GameDifficulty = GameDifficulty.EASY,
    val pieceCount: Int = 120,
    val selectedStoryId: String = StoryPresets.defaultStoryId,
    val selectedStoryPageIndex: Int = 0,
    val selectedTheme: ThemeData? = StoryPresets.pages.first().theme,
    val pieces: List<PuzzlePiece> = emptyList(),
    val puzzleBitmap: ImageBitmap? = null,
    val pieceBitmaps: Map<String, ImageBitmap> = emptyMap(),
    val isImageLoading: Boolean = false,
    val gridCols: Int = 0,
    val gridRows: Int = 0,
    val correctPositions: Map<String, Pair<Int, Int>> = emptyMap(),
    val cellFilledBy: Map<String, String> = emptyMap(),
    val showCelebration: Boolean = false,
    val wrongDropHint: Boolean = false,
    val showPositionHint: Boolean = true,
    val elapsedSeconds: Long = 0,
    val isPaused: Boolean = false,
    val errorMessage: String? = null
)
