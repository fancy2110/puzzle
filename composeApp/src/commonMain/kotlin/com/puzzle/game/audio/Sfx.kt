package com.puzzle.game.audio

import androidx.compose.runtime.staticCompositionLocalOf

/** Sound effects available across the app. */
enum class Sfx {
    PieceSelect,
    PiecePlace,
    PieceWrong,
    Complete,
    ButtonClick
}

/** Plays a sound effect. Implemented on each platform via [SoundManager]. */
fun interface SfxPlayer {
    fun play(sfx: Sfx)
}

/** Default no-op player, replaced by a real one at the app root. */
val LocalSfxPlayer = staticCompositionLocalOf<SfxPlayer> {
    SfxPlayer { }
}
