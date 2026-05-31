package com.puzzle.game

/**
 * Returns the platform's cache directory for storing temporary files
 * (e.g., puzzle piece PNGs saved by the Rust engine).
 */
expect fun platformCacheDir(): String
