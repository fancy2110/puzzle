package com.puzzle.game.game

import androidx.compose.ui.graphics.ImageBitmap
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
    COMPLETED
}

data class GameState(
    val phase: GamePhase = GamePhase.MENU,
    val difficulty: GameDifficulty = GameDifficulty.EASY,
    val selectedTheme: ThemeData? = null,
    val pieces: List<PuzzlePiece> = emptyList(),
    val placedPieces: Set<String> = emptySet(),
    val puzzleBitmap: ImageBitmap? = null,
    val isImageLoading: Boolean = false
) {
    val isComplete: Boolean
        get() = phase == GamePhase.COMPLETED ||
                (pieces.isNotEmpty() && placedPieces.size == pieces.size)
}
