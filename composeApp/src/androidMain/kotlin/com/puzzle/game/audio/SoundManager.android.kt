package com.puzzle.game.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool

actual class SoundManager(context: Context) {
    private val soundPool: SoundPool
    private val soundIds = mutableMapOf<String, Int>()

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

    // In a production app, load from res/raw/.
    // For now these are no-ops — real sound files would be added as assets.
    actual fun playPieceSelect() = Unit
    actual fun playPiecePlace() = Unit
    actual fun playPieceWrong() = Unit
    actual fun playComplete() = Unit
    actual fun playButtonClick() = Unit

    actual fun release() {
        soundPool.release()
    }
}
