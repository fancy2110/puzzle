package com.puzzle.game.audio

actual class SoundManager {
    /** TODO(v1.1): Implement with AVAudioPlayer — load .caf/.m4a from bundle.
     *  @see SoundManager.android.kt for Android SoundPool plan. */
    actual fun playPieceSelect() = Unit
    actual fun playPiecePlace() = Unit
    actual fun playPieceWrong() = Unit
    actual fun playComplete() = Unit
    actual fun playButtonClick() = Unit
    actual fun release() = Unit
}
