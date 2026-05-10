package com.puzzle.game.game

import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.puzzle.game.data.PuzzlePictureGenerator
import com.puzzle.game.data.ThemeData
import com.puzzle.game.data.ThemePresets
import com.puzzle.game.engine.PuzzleEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GameViewModel : ViewModel() {
    private val _state = MutableStateFlow(GameState())
    val state: StateFlow<GameState> = _state

    private val engine = PuzzleEngine()

    val themes: List<ThemeData> = ThemePresets.themes

    fun selectTheme(themeId: String) {
        val theme = ThemePresets.getById(themeId)
        _state.update { it.copy(selectedTheme = theme) }
    }

    fun selectDifficulty(difficulty: GameDifficulty) {
        _state.update { it.copy(difficulty = difficulty) }
    }

    fun startGame() {
        val currentState = _state.value
        val theme = currentState.selectedTheme ?: ThemePresets.themes.first()

        _state.update { it.copy(phase = GamePhase.GENERATING, isImageLoading = true) }

        viewModelScope.launch {
            val puzzleBitmap = PuzzlePictureGenerator.generate(theme, 800, 600)

            engine.loadImage(puzzleBitmap.width, puzzleBitmap.height)
            engine.splitImage(
                pieceCount = currentState.difficulty.pieceCount,
                blockSize = 64
            )
            val shuffledPieces = engine.shufflePieces()

            _state.update {
                it.copy(
                    phase = GamePhase.PLAYING,
                    pieces = shuffledPieces,
                    puzzleBitmap = puzzleBitmap,
                    isImageLoading = false
                )
            }
        }
    }

    fun placePiece(pieceId: String) {
        _state.update { current ->
            val newPlaced = current.placedPieces + pieceId
            if (newPlaced.size == current.pieces.size) {
                current.copy(placedPieces = newPlaced, phase = GamePhase.COMPLETED)
            } else {
                current.copy(placedPieces = newPlaced)
            }
        }
    }

    fun resetGame() {
        _state.update { GameState() }
    }

    fun goToMenu() {
        _state.update {
            it.copy(
                phase = GamePhase.MENU,
                pieces = emptyList(),
                placedPieces = emptySet(),
                puzzleBitmap = null
            )
        }
    }
}
