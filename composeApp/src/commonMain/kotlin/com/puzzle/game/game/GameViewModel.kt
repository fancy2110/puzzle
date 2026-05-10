package com.puzzle.game.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.puzzle.game.engine.PuzzleEngine
import com.puzzle.game.engine.model.PuzzlePiece
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GameViewModel : ViewModel() {
    private val _state = MutableStateFlow(GameState())
    val state: StateFlow<GameState> = _state

    private val engine = PuzzleEngine()

    fun selectDifficulty(difficulty: GameDifficulty) {
        _state.update { it.copy(difficulty = difficulty) }
    }

    fun startGame() {
        val currentState = _state.value
        _state.update { it.copy(phase = GamePhase.GENERATING, isImageLoading = true) }

        viewModelScope.launch {
            val imageSource = currentState.imageSource
            if (imageSource != null) {
                engine.loadImage(800, 600)
                engine.splitImage(pieceCount = currentState.difficulty.pieceCount, blockSize = 64)
                val shuffledPieces = engine.shufflePieces()

                _state.update {
                    it.copy(
                        phase = GamePhase.PLAYING,
                        pieces = shuffledPieces,
                        isImageLoading = false
                    )
                }
            }
        }
    }

    fun setImageSource(source: String) {
        _state.update { it.copy(imageSource = source) }
    }

    fun setPrompt(prompt: String) {
        _state.update { it.copy(promptText = prompt) }
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
        _state.update { it.copy(phase = GamePhase.MENU, pieces = emptyList(), placedPieces = emptySet()) }
    }
}
