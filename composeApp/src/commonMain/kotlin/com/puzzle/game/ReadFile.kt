package com.puzzle.game

/**
 * Read a file from the platform's filesystem as raw bytes.
 * Used to load local images (e.g. AI results saved to disk).
 */
expect fun readFileBytes(path: String): ByteArray?
