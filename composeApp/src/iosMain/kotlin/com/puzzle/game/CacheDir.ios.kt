package com.puzzle.game

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask

/**
 * iOS: returns the standard Caches directory (Library/Caches).
 * Not backed up by iCloud, persists during app runtime, cleaned by system when needed.
 */
@OptIn(ExperimentalForeignApi::class)
actual fun platformCacheDir(): String {
    return NSSearchPathForDirectoriesInDomains(
        NSCachesDirectory, NSUserDomainMask, true
    ).first() as String
}
