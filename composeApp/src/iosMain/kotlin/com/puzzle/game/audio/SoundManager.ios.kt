package com.puzzle.game.audio

actual class SoundManager {
    // iOS implementation — would use AVAudioPlayer.
    // For now these are no-ops.
    actual fun playPieceSelect() = Unit
    actual fun playPiecePlace() = Unit
    actual fun playPieceWrong() = Unit
    actual fun playComplete() = Unit
    actual fun playButtonClick() = Unit
    actual fun release() = Unit
}
