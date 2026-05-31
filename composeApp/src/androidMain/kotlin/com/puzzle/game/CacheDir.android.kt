package com.puzzle.game

import android.content.Context

private var appCtx: Context? = null

fun initCacheDir(context: Context) {
    appCtx = context.applicationContext
}

actual fun platformCacheDir(): String {
    val ctx = appCtx ?: return "/data/local/tmp/puzzle"
    return ctx.cacheDir.absolutePath + "/puzzle_pieces"
}
