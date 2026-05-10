package com.puzzle.game.game

import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.puzzle.game.data.PuzzlePictureGenerator
import com.puzzle.game.data.ThemeData
import com.puzzle.game.data.ThemePresets
import com.puzzle.game.engine.PuzzleEngine
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GameViewModel : ViewModel() {
    private val _state = MutableStateFlow(GameState())
    val state: StateFlow<GameState> = _state

    private val engine = PuzzleEngine()
    val dragDropState = DragDropState()

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

            val imageWidth = puzzleBitmap.width
            val imageHeight = puzzleBitmap.height
            val gridCols = (imageWidth / 64) + 1
            val gridRows = (imageHeight / 64) + 1

            val correctPositions = mutableMapOf<String, Pair<Int, Int>>()
            for (piece in shuffledPieces) {
                if (piece.items.isNotEmpty()) {
                    val center = piece.items[piece.items.size / 2]
                    correctPositions[piece.id] = Pair(center.y, center.x)
                }
            }

            _state.update {
                it.copy(
                    phase = GamePhase.PLAYING,
                    pieces = shuffledPieces,
                    puzzleBitmap = puzzleBitmap,
                    gridCols = gridCols,
                    gridRows = gridRows,
                    correctPositions = correctPositions,
                    cellFilledBy = mutableMapOf(),
                    isImageLoading = false,
                    showCelebration = false
                )
            }
        }
    }

    fun tryPlacePiece(pieceId: String, row: Int, col: Int) {
        val currentState = _state.value
        val expectedPos = currentState.correctPositions[pieceId] ?: return
        val expectedRow = expectedPos.first
        val expectedCol = expectedPos.second

        val isNearCorrect = (row == expectedRow && col == expectedCol) ||
                ((row - expectedRow) in -1..1 && (col - expectedCol) in -1..1)

        if (isNearCorrect) {
            _state.update { current ->
                val newCellFilled = current.cellFilledBy.toMutableMap()
                newCellFilled["${expectedRow}_${expectedCol}"] = pieceId

                val allPlaced = current.correctPositions.values.all { pos ->
                    newCellFilled["${pos.first}_${pos.second}"] != null
                }

                if (allPlaced) {
                    current.copy(
                        cellFilledBy = newCellFilled,
                        phase = GamePhase.COMPLETED,
                        showCelebration = true
                    )
                } else {
                    current.copy(cellFilledBy = newCellFilled)
                }
            }
        } else {
            _state.update { it.copy(wrongDropHint = true) }
            viewModelScope.launch {
                delay(600)
                _state.update { it.copy(wrongDropHint = false) }
            }
        }
    }

    fun handleDragEnd() {
        val result = dragDropState.endDrag()
        if (result != null) {
            tryPlacePiece(result.pieceId, result.targetRow, result.targetCol)
        } else {
            dragDropState.cancelDrag()
        }
    }

    fun placePiece(pieceId: String) {
        _state.update { current ->
            val piece = current.pieces.firstOrNull { it.id == pieceId }
            val pos = piece?.items?.firstOrNull()
            val newCellFilled = current.cellFilledBy.toMutableMap()
            if (pos != null) {
                newCellFilled["${pos.y}_${pos.x}"] = pieceId
            }

            val allPlaced = current.correctPositions.values.all { p ->
                newCellFilled["${p.first}_${p.second}"] != null
            }

            if (allPlaced) {
                current.copy(
                    cellFilledBy = newCellFilled,
                    phase = GamePhase.COMPLETED,
                    showCelebration = true
                )
            } else {
                current.copy(cellFilledBy = newCellFilled)
            }
        }
    }

    fun dismissCelebration() {
        _state.update { it.copy(showCelebration = false) }
    }

    fun resetGame() {
        _state.update { GameState() }
    }

    fun goToMenu() {
        _state.update {
            it.copy(
                phase = GamePhase.MENU,
                pieces = emptyList(),
                cellFilledBy = mutableMapOf(),
                correctPositions = emptyMap(),
                puzzleBitmap = null
            )
        }
    }
}
