package com.puzzle.game

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import com.puzzle.game.data.PreferencesFactory
import com.puzzle.game.game.GameViewModel
import com.puzzle.game.navigation.NavigationViewModel
import com.puzzle.game.navigation.Screen
import com.puzzle.game.ui.GameScreen
import com.puzzle.game.ui.ImageSourceScreen
import com.puzzle.game.ui.MenuScreen
import com.puzzle.game.ui.SettingsScreen
import com.puzzle.game.ui.SplashScreen
import com.puzzle.game.ui.ThemeScreen
import com.puzzle.game.ui.theme.PuzzleGameTheme

@Composable
fun App() {
    PuzzleGameTheme {
        val navViewModel = remember { NavigationViewModel() }
        val gameViewModel: GameViewModel = viewModel { GameViewModel() }
        val preferences = remember { PreferencesFactory.create() }

        val screenStack by navViewModel.screenStack.collectAsState()

        when (screenStack.lastOrNull()) {
            Screen.Splash -> {
                SplashScreen(
                    onFinished = {
                        navViewModel.replaceWith(Screen.Menu)
                    }
                )
            }

            Screen.Menu -> {
                MenuScreen(
                    viewModel = gameViewModel,
                    onStartGame = {
                        gameViewModel.startGame()
                        navViewModel.navigateTo(Screen.Game)
                    },
                    onPickTheme = {
                        navViewModel.navigateTo(Screen.ThemePicker)
                    },
                    onPickImage = {
                        navViewModel.navigateTo(Screen.ImageSource)
                    },
                    onOpenSettings = {
                        navViewModel.navigateTo(Screen.Settings)
                    }
                )
            }

            Screen.ThemePicker -> {
                ThemeScreen(
                    viewModel = gameViewModel,
                    onBack = { navViewModel.goBack() },
                    onConfirm = { navViewModel.goBack() }
                )
            }

            Screen.ImageSource -> {
                ImageSourceScreen(
                    onBack = { navViewModel.goBack() },
                    onPickBuiltIn = { navViewModel.navigateTo(Screen.ThemePicker) },
                    onUseCurrent = { navViewModel.goBackTo(Screen.Menu) },
                    onGenerateAi = {
                        gameViewModel.startGame()
                        navViewModel.navigateTo(Screen.Game)
                    }
                )
            }

            Screen.Settings -> {
                SettingsScreen(
                    preferences = preferences,
                    onBack = { navViewModel.goBack() },
                    onOpenImageSource = { navViewModel.navigateTo(Screen.ImageSource) }
                )
            }

            Screen.Game -> {
                GameScreen(
                    viewModel = gameViewModel,
                    onGoToMenu = {
                        gameViewModel.goToMenu()
                        navViewModel.replaceWith(Screen.Menu)
                    },
                    onPlayAgain = {
                        gameViewModel.startGame()
                    }
                )
            }

            null -> {
                // Fallback — shouldn't happen
                SplashScreen(
                    onFinished = {
                        navViewModel.replaceWith(Screen.Menu)
                    }
                )
            }
        }
    }
}
