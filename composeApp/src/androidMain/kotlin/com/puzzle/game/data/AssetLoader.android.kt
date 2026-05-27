package com.puzzle.game.data

import android.content.Context

/**
 * Android: load from assets/ folder.
 * Requires a Context — set via AssetLoaderAndroid.init(context) at app startup.
 */
actual object AssetLoader {
    private var appContext: Context? = null

    /** Must be called once, e.g. in MainActivity.onCreate() */
    fun init(context: Context) {
        appContext = context.applicationContext
    }

    actual fun readBytes(path: String): ByteArray? {
        val ctx = appContext ?: return null
        return try {
            ctx.assets.open(path).use { it.readBytes() }
        } catch (e: Exception) {
            null
        }
    }
}
