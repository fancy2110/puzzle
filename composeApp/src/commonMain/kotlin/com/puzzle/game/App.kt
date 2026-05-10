package com.puzzle.game

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.puzzle.game.game.GamePhase
import com.puzzle.game.game.GameViewModel
import com.puzzle.game.ui.GameScreen
import com.puzzle.game.ui.MenuScreen
import com.puzzle.game.ui.theme.PuzzleGameTheme

@Composable
fun App() {
    PuzzleGameTheme {
        val viewModel: GameViewModel = viewModel { GameViewModel() }
        val state = viewModel.state

        when (state.value.phase) {
            GamePhase.MENU -> MenuScreen(viewModel)
            else -> GameScreen(viewModel)
        }
    }
}
