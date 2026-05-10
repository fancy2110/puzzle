package com.puzzle.game.engine.model

data class Rect(
    var left: Int = 0,
    var top: Int = 0,
    var right: Int = 0,
    var bottom: Int = 0
) {
    val width: Int get() = right - left
    val height: Int get() = bottom - top
    val size: Pair<Int, Int> get() = Pair(width, height)
    val area: Int get() = width * height

    fun expand(l: Int, t: Int, r: Int, b: Int) {
        if (l < left) left = l
        if (t < top) top = t
        if (r > right) right = r
        if (b > bottom) bottom = b
    }
}
