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
    private var soundEnabled = false

    init {
        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(4)
            .setAudioAttributes(attrs)
            .build()

        loadSoundEffects()
    }

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
        if (soundEnabled && !player.isPlaying) {
            player.start()
        }
    }

    private fun createBackgroundPlayer(): MediaPlayer {
        val musicFile = copyPackagedAssetToCache(BACKGROUND_MUSIC_FILE)
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

    private fun loadSoundEffects() {
        SFX_FILES.forEach { fileName ->
            try {
                val file = copyPackagedAssetToCache(fileName)
                val soundId = soundPool.load(file.absolutePath, 1)
                soundIds[fileName] = soundId
            } catch (error: Exception) {
                PuzzleLog.e("SoundManager", "Unable to load $fileName", error)
            }
        }
    }

    actual fun playPieceSelect() = playSfx(SFX_SELECT)
    actual fun playPiecePlace() = playSfx(SFX_PLACE)
    actual fun playPieceWrong() = playSfx(SFX_WRONG)
    actual fun playComplete() = playSfx(SFX_COMPLETE)
    actual fun playButtonClick() = playSfx(SFX_CLICK)

    private fun playSfx(fileName: String) {
        if (!soundEnabled) return
        val soundId = soundIds[fileName] ?: return
        soundPool.play(soundId, SFX_VOLUME, SFX_VOLUME, 1, 0, 1f)
    }

    /**
     * Copy a packaged Compose resource (from assets) to the cache dir so both
     * MediaPlayer and SoundPool can access it via a filesystem path.
     */
    private fun copyPackagedAssetToCache(fileName: String): File {
        val target = File(appContext.cacheDir, fileName)
        if (target.exists() && target.length() > 0L) return target

        target.parentFile?.mkdirs()
        val assetPath = "$PACKAGED_RESOURCES_ASSET_ROOT/$fileName"
        appContext.assets.open(assetPath).use { input ->
            target.outputStream().use { output -> input.copyTo(output) }
        }
        return target
    }

    actual fun release() {
        backgroundPlayer?.release()
        backgroundPlayer = null
        soundPool.release()
        soundIds.clear()
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

private const val SFX_VOLUME = 0.55f
private const val BACKGROUND_MUSIC_FILE = "audio/storybook_lullaby.wav"
private const val SFX_SELECT = "audio/sfx_select.wav"
private const val SFX_PLACE = "audio/sfx_place.wav"
private const val SFX_WRONG = "audio/sfx_wrong.wav"
private const val SFX_COMPLETE = "audio/sfx_complete.wav"
private const val SFX_CLICK = "audio/sfx_click.wav"
private val SFX_FILES = listOf(
    SFX_SELECT,
    SFX_PLACE,
    SFX_WRONG,
    SFX_COMPLETE,
    SFX_CLICK
)
private const val PACKAGED_RESOURCES_ASSET_ROOT =
    "composeResources/puzzlegame.composeapp.generated.resources/files"
