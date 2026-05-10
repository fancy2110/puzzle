package com.puzzle.game.game

import com.puzzle.game.engine.model.PuzzlePiece

enum class GameDifficulty(val pieceCount: Int, val label: String) {
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
    val pieces: List<PuzzlePiece> = emptyList(),
    val placedPieces: Set<String> = emptySet(),
    val isImageLoading: Boolean = false,
    val promptText: String = "",
    val imageSource: String? = null
) {
    val isComplete: Boolean
        get() = phase == GamePhase.COMPLETED ||
                (pieces.isNotEmpty() && placedPieces.size == pieces.size)
}
