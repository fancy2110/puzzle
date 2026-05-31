package com.puzzle.game

import android.content.Context

private var appCtx: Context? = null

fun initCacheDir(context: Context) {
    appCtx = context.applicationContext
}

/**
 * Android: returns [Context.cacheDir] — the system-managed cache directory.
 * Files may be deleted when the device is low on storage.
 */
actual fun platformCacheDir(): String {
    val ctx = appCtx ?: return "/data/local/tmp"
    return ctx.cacheDir.absolutePath
}
