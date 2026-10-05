package com.puzzle.game.audio

import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFAudio.AVAudioPlayer
import platform.Foundation.NSURL
import puzzlegame.composeapp.generated.resources.Res

@OptIn(ExperimentalForeignApi::class)
actual class SoundManager {
    private var backgroundPlayer: AVAudioPlayer? = null
    private val effectPlayers = mutableMapOf<String, AVAudioPlayer>()
    private var soundEnabled = false

    actual fun setBackgroundMusicEnabled(enabled: Boolean) {
        soundEnabled = enabled
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
        if (soundEnabled) {
            startBackgroundMusic()
        }
    }

    private fun startBackgroundMusic() {
        val player = backgroundPlayer ?: createBackgroundPlayer().also {
            backgroundPlayer = it
        }
        if (soundEnabled && !player.playing) {
            player.play()
        }
    }

    private fun createBackgroundPlayer(): AVAudioPlayer {
        return createPlayer(BACKGROUND_MUSIC_PATH).apply {
            numberOfLoops = -1
            volume = 0.24f
            prepareToPlay()
        }
    }

    actual fun playPieceSelect() = playEffect(SFX_SELECT)
    actual fun playPiecePlace() = playEffect(SFX_PLACE)
    actual fun playPieceWrong() = playEffect(SFX_WRONG)
    actual fun playComplete() = playEffect(SFX_COMPLETE)
    actual fun playButtonClick() = playEffect(SFX_CLICK)

    private fun playEffect(path: String) {
        if (!soundEnabled) return
        val player = effectPlayers[path]
            ?: createPlayer(path).also { effectPlayers[path] = it }
        // Restart if the same effect is already playing, so rapid taps are heard.
        player.currentTime = 0.0
        player.play()
    }

    private fun createPlayer(path: String): AVAudioPlayer {
        val url = NSURL.URLWithString(Res.getUri("files/$path"))
            ?: error("Missing audio resource: $path")
        return AVAudioPlayer(contentsOfURL = url, error = null).apply {
            volume = SFX_VOLUME
            prepareToPlay()
        }
    }

    actual fun release() {
        backgroundPlayer?.stop()
        backgroundPlayer = null
        effectPlayers.values.forEach { it.stop() }
        effectPlayers.clear()
    }
}

actual object SoundManagerFactory {
    actual fun create(): SoundManager = SoundManager()
}

private const val SFX_VOLUME = 0.55f
private const val BACKGROUND_MUSIC_PATH = "audio/storybook_lullaby.wav"
private const val SFX_SELECT = "audio/sfx_select.wav"
private const val SFX_PLACE = "audio/sfx_place.wav"
private const val SFX_WRONG = "audio/sfx_wrong.wav"
private const val SFX_COMPLETE = "audio/sfx_complete.wav"
private const val SFX_CLICK = "audio/sfx_click.wav"
