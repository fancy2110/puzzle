package com.puzzle.game

/**
 * Returns the platform's standard cache directory.
 *
 * Android: [Context.cacheDir] — auto-cleared on low storage.
 * iOS:     NSCachesDirectory — not iCloud-backed, persists during runtime.
 *
 * Used for temporary files like puzzle piece PNGs saved by the Rust engine.
 */
expect fun platformCacheDir(): String
