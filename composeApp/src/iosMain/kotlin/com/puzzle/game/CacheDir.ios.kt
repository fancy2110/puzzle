package com.puzzle.game

import platform.Foundation.NSTemporaryDirectory

actual fun platformCacheDir(): String {
    return NSTemporaryDirectory() + "puzzle_pieces"
}
