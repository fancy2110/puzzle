package com.puzzle.game

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
