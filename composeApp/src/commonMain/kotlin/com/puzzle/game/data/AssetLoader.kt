package com.puzzle.game.data

/**
 * Cross-platform loader for built-in asset files.
 * Android: loads from assets/ folder
 * iOS: loads from NSBundle
 */
expect object AssetLoader {
    /**
     * Read a built-in asset as raw bytes.
     * Returns null if the asset doesn't exist.
     */
    fun readBytes(path: String): ByteArray?
}
