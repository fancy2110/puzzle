package com.puzzle.game

/**
 * Read a file from the platform's filesystem as raw bytes.
 * Used to load puzzle piece PNGs saved by the Rust engine.
 */
expect fun readFileBytes(path: String): ByteArray?
