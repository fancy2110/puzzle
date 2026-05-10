package com.puzzle.game.engine.model

data class Block(
    val position: Position,
    val rect: Rect,
    var color: Long = 0L,
    var isTaken: Boolean = false
) {
    fun taken() {
        isTaken = true
    }
}
