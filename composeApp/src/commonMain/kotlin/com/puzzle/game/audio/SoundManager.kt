package com.puzzle.game.audio

/**
 * Cross-platform sound effects.
 * Uses expect/actual for platform-specific implementations.
 */
expect class SoundManager {
    fun playPieceSelect()
    fun playPiecePlace()
    fun playPieceWrong()
    fun playComplete()
    fun playButtonClick()
    fun release()
}
