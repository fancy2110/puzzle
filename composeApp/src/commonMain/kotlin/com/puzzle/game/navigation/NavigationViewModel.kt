package com.puzzle.game.navigation

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Simple stack-based navigation manager.
 * Manages the main screen stack independently of GameState.
 */
class NavigationViewModel {
    private val _screenStack = MutableStateFlow<List<Screen>>(listOf(Screen.Splash))
    val screenStack: StateFlow<List<Screen>> = _screenStack.asStateFlow()

    val currentScreen: Screen
        get() = _screenStack.value.lastOrNull() ?: Screen.Menu

    fun navigateTo(screen: Screen) {
        if (_screenStack.value.lastOrNull() == screen) return
        _screenStack.value = _screenStack.value + screen
    }

    /**
     * Replace the entire stack with one screen (e.g., splash → menu after timeout).
     */
    fun replaceWith(screen: Screen) {
        _screenStack.value = listOf(screen)
    }

    fun goBack(): Boolean {
        val stack = _screenStack.value
        if (stack.size <= 1) return false
        _screenStack.value = stack.dropLast(1)
        return true
    }

    fun goBackTo(screen: Screen) {
        val stack = _screenStack.value
        val index = stack.lastIndexOf(screen)
        if (index >= 0) {
            _screenStack.value = stack.subList(0, index + 1)
        }
    }
}
