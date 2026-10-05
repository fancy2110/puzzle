package com.puzzle.game

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.puzzle.game.analytics.Analytics
import com.puzzle.game.analytics.AnalyticsScreen
import com.puzzle.game.audio.SoundManagerFactory
import com.puzzle.game.data.PreferencesFactory
import com.puzzle.game.game.GameViewModel
import com.puzzle.game.i18n.AppLanguage
import com.puzzle.game.i18n.LocalAppLanguage
import com.puzzle.game.i18n.LocalAppStrings
import com.puzzle.game.i18n.stringsFor
import com.puzzle.game.navigation.NavigationViewModel
import com.puzzle.game.navigation.Screen
import com.puzzle.game.ui.GameScreen
import com.puzzle.game.ui.ImageSourceScreen
import com.puzzle.game.ui.MenuScreen
import com.puzzle.game.ui.SettingsScreen
import com.puzzle.game.ui.SplashScreen
import com.puzzle.game.ui.ThemeScreen
import com.puzzle.game.ui.theme.PuzzleGameTheme
import kotlin.time.TimeSource

@Composable
fun App() {
    PuzzleGameTheme {
        val navViewModel = remember { NavigationViewModel() }
        val gameViewModel: GameViewModel = viewModel { GameViewModel() }
        val preferences = remember { PreferencesFactory.create() }
        val soundManager = remember { SoundManagerFactory.create() }
        var soundEnabled by remember { mutableStateOf(preferences.isSoundEnabled()) }
        var language by remember {
            mutableStateOf(AppLanguage.fromCode(preferences.getLanguageCode()))
        }
        val appStartedAt = remember { TimeSource.Monotonic.markNow() }
        val lifecycleOwner = LocalLifecycleOwner.current

        LaunchedEffect(language) {
            Analytics.appLaunch(language.code)
            Analytics.setLanguage(language.code)
        }

        LaunchedEffect(soundEnabled) {
            soundManager.setBackgroundMusicEnabled(soundEnabled)
        }

        DisposableEffect(soundManager) {
            onDispose {
                soundManager.release()
            }
        }

        DisposableEffect(lifecycleOwner, soundManager) {
            val observer = LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_RESUME -> soundManager.resumeBackgroundMusic()
                    Lifecycle.Event.ON_PAUSE,
                    Lifecycle.Event.ON_STOP -> soundManager.pauseBackgroundMusic()
                    else -> Unit
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
            }
        }

        val screenStack by navViewModel.screenStack.collectAsState()
        val currentScreen = screenStack.lastOrNull()

        DisposableEffect(currentScreen) {
            val analyticsScreen = currentScreen?.analyticsScreen()
            val sourceScreen = screenStack.dropLast(1).lastOrNull()?.analyticsScreen()?.id
            val enteredAt = TimeSource.Monotonic.markNow()
            analyticsScreen?.let { Analytics.screen(it, source = sourceScreen) }
            onDispose {
                analyticsScreen?.let {
                    Analytics.screenDuration(
                        screen = it,
                        durationMs = enteredAt.elapsedNow().inWholeMilliseconds
                    )
                }
            }
        }

        CompositionLocalProvider(
            LocalAppLanguage provides language,
            LocalAppStrings provides stringsFor(language)
        ) {
            when (currentScreen) {
                Screen.Splash -> {
                    SplashScreen(
                        onFinished = {
                            Analytics.appReady(appStartedAt.elapsedNow().inWholeMilliseconds)
                            navViewModel.replaceWith(Screen.Menu)
                        }
                    )
                }

                Screen.Menu -> {
                    MenuScreen(
                        viewModel = gameViewModel,
                        onStartGame = {
                            Analytics.click("start_game", AnalyticsScreen.Menu)
                            gameViewModel.startGame()
                            navViewModel.navigateTo(Screen.Game)
                        },
                        onPickTheme = {
                            Analytics.click("open_theme_picker", AnalyticsScreen.Menu)
                            navViewModel.navigateTo(Screen.ThemePicker)
                        },
                        onPickImage = {
                            Analytics.click("open_image_source", AnalyticsScreen.Menu)
                            navViewModel.navigateTo(Screen.ImageSource)
                        },
                        onOpenSettings = {
                            Analytics.click("open_settings", AnalyticsScreen.Menu)
                            navViewModel.navigateTo(Screen.Settings)
                        }
                    )
                }

                Screen.ThemePicker -> {
                    val launchedFromImageSource = screenStack.dropLast(1).lastOrNull() == Screen.ImageSource
                    ThemeScreen(
                        viewModel = gameViewModel,
                        onBack = {
                            Analytics.click("back", AnalyticsScreen.ThemePicker)
                            navViewModel.goBack()
                        },
                        onConfirm = {
                            Analytics.click("confirm_theme", AnalyticsScreen.ThemePicker)
                            if (launchedFromImageSource) {
                                navViewModel.goBackTo(Screen.Menu)
                            } else {
                                navViewModel.goBack()
                            }
                        }
                    )
                }

                Screen.ImageSource -> {
                    ImageSourceScreen(
                        onBack = {
                            Analytics.click("back", AnalyticsScreen.ImageSource)
                            navViewModel.goBack()
                        },
                        onPickBuiltIn = {
                            Analytics.click("image_source_builtin", AnalyticsScreen.ImageSource)
                            navViewModel.navigateTo(Screen.ThemePicker)
                        },
                        onUseCurrent = {
                            Analytics.click("image_source_current_theme", AnalyticsScreen.ImageSource)
                            navViewModel.goBackTo(Screen.Menu)
                        }
                    )
                }

                Screen.Settings -> {
                    SettingsScreen(
                        preferences = preferences,
                        language = language,
                        soundEnabled = soundEnabled,
                        onSoundEnabledChange = { enabled ->
                            soundEnabled = enabled
                            preferences.setSoundEnabled(enabled)
                        },
                        onLanguageChange = { selectedLanguage ->
                            language = selectedLanguage
                            preferences.setLanguageCode(selectedLanguage.code)
                            Analytics.click(
                                target = "change_language",
                                screen = AnalyticsScreen.Settings,
                                properties = mapOf("language" to selectedLanguage.code)
                            )
                        },
                        onBack = {
                            Analytics.click("back", AnalyticsScreen.Settings)
                            navViewModel.goBack()
                        },
                        onOpenImageSource = {
                            Analytics.click("settings_open_image_source", AnalyticsScreen.Settings)
                            navViewModel.navigateTo(Screen.ImageSource)
                        }
                    )
                }

                Screen.Game -> {
                    GameScreen(
                        viewModel = gameViewModel,
                        onGoToMenu = {
                            Analytics.click("go_to_menu", AnalyticsScreen.Game)
                            gameViewModel.goToMenu()
                            navViewModel.replaceWith(Screen.Menu)
                        },
                        onContinueStory = {
                            Analytics.click("next_story_page", AnalyticsScreen.Game)
                            gameViewModel.startNextStoryPage()
                        },
                        onChooseStory = {
                            Analytics.click("choose_another_story", AnalyticsScreen.Game)
                            gameViewModel.goToMenu()
                            navViewModel.replaceWith(Screen.Menu)
                            navViewModel.navigateTo(Screen.ThemePicker)
                        }
                    )
                }

                null -> {
                    SplashScreen(
                        onFinished = {
                            Analytics.appReady(appStartedAt.elapsedNow().inWholeMilliseconds)
                            navViewModel.replaceWith(Screen.Menu)
                        }
                    )
                }
            }
        }
    }
}

private fun Screen.analyticsScreen(): AnalyticsScreen = when (this) {
    Screen.Splash -> AnalyticsScreen.Splash
    Screen.Menu -> AnalyticsScreen.Menu
    Screen.ThemePicker -> AnalyticsScreen.ThemePicker
    Screen.ImageSource -> AnalyticsScreen.ImageSource
    Screen.Settings -> AnalyticsScreen.Settings
    Screen.Game -> AnalyticsScreen.Game
}
