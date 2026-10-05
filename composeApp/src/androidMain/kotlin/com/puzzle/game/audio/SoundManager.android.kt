package com.puzzle.game.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import com.puzzle.logger.PuzzleLog
import java.io.File

actual class SoundManager(context: Context) {
    private val appContext = context.applicationContext
    private val soundPool: SoundPool
    private val soundIds = mutableMapOf<String, Int>()
    private var backgroundPlayer: MediaPlayer? = null
    private var backgroundEnabled = false

    init {
        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(4)
            .setAudioAttributes(attrs)
            .build()
    }

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
        if (backgroundEnabled && !player.isPlaying) {
            player.start()
        }
    }

    private fun createBackgroundPlayer(): MediaPlayer {
        val musicFile = copyBackgroundMusicToCache()
        return MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            setDataSource(musicFile.absolutePath)
            isLooping = true
            setVolume(0.24f, 0.24f)
            setOnErrorListener { _, what, extra ->
                PuzzleLog.e("SoundManager", "Background music error: $what/$extra")
                false
            }
            prepare()
        }
    }

    private fun copyBackgroundMusicToCache(): File {
        val target = File(appContext.cacheDir, BACKGROUND_MUSIC_FILE)
        if (target.exists() && target.length() > 0L) return target

        appContext.assets.open(BACKGROUND_MUSIC_ASSET).use { input ->
            target.outputStream().use { output -> input.copyTo(output) }
        }
        return target
    }

    /** TODO(v1.1): Implement with SoundPool — load .ogg/.wav from res/raw/.
     *  @see SoundManager.ios.kt for iOS AVAudioPlayer plan. */
    actual fun playPieceSelect() = Unit
    actual fun playPiecePlace() = Unit
    actual fun playPieceWrong() = Unit
    actual fun playComplete() = Unit
    actual fun playButtonClick() = Unit

    actual fun release() {
        backgroundPlayer?.release()
        backgroundPlayer = null
        soundPool.release()
    }
}

actual object SoundManagerFactory {
    private var context: Context? = null

    fun init(context: Context) {
        this.context = context.applicationContext
    }

    actual fun create(): SoundManager {
        val ctx = context
            ?: throw IllegalStateException("SoundManagerFactory.init(context) must be called before create()")
        return SoundManager(ctx)
    }
}

private const val BACKGROUND_MUSIC_FILE = "storybook_lullaby.wav"
private const val BACKGROUND_MUSIC_ASSET =
    "composeResources/puzzlegame.composeapp.generated.resources/files/audio/storybook_lullaby.wav"
