package com.puzzle.game.engine.model

data class Position(val y: Int, val x: Int) {
    companion object {
        val ZERO = Position(0, 0)
    }
}
