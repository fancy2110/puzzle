package com.puzzle.game.audio

import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFAudio.AVAudioPlayer
import platform.Foundation.NSURL
import puzzlegame.composeapp.generated.resources.Res

@OptIn(ExperimentalForeignApi::class)
actual class SoundManager {
    private var backgroundPlayer: AVAudioPlayer? = null
    private var backgroundEnabled = false

    actual fun setBackgroundMusicEnabled(enabled: Boolean) {
        backgroundEnabled = enabled
        if (enabled) {
            startBackgroundMusic()
        } else {
            backgroundPlayer?.pause()
        }
    }

    actual fun pauseBackgroundMusic() {
        backgroundPlayer?.pause()
    }

    actual fun resumeBackgroundMusic() {
        if (backgroundEnabled) {
            startBackgroundMusic()
        }
    }

    private fun startBackgroundMusic() {
        val player = backgroundPlayer ?: createBackgroundPlayer().also {
            backgroundPlayer = it
        }
        if (backgroundEnabled && !player.playing) {
            player.play()
        }
    }

    private fun createBackgroundPlayer(): AVAudioPlayer {
        val url = NSURL.URLWithString(Res.getUri("files/audio/storybook_lullaby.wav"))
            ?: error("Missing background music resource")
        return AVAudioPlayer(contentsOfURL = url, error = null).apply {
            numberOfLoops = -1
            volume = 0.24f
            prepareToPlay()
        }
    }

    /** TODO(v1.1): Implement with AVAudioPlayer — load .caf/.m4a from bundle.
     *  @see SoundManager.android.kt for Android SoundPool plan. */
    actual fun playPieceSelect() = Unit
    actual fun playPiecePlace() = Unit
    actual fun playPieceWrong() = Unit
    actual fun playComplete() = Unit
    actual fun playButtonClick() = Unit
    actual fun release() {
        backgroundPlayer?.stop()
        backgroundPlayer = null
    }
}

actual object SoundManagerFactory {
    actual fun create(): SoundManager = SoundManager()
}
