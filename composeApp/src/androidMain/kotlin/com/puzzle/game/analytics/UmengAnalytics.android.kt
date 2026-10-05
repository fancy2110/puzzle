package com.puzzle.game.analytics

import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import com.puzzle.logger.PuzzleLog
import com.umeng.analytics.MobclickAgent
import com.umeng.commonsdk.UMConfigure

actual object PlatformAnalytics {
    private var appContext: Context? = null
    private var appKey: String = ""
    private var channel: String = "official"
    private var analyticsEnabled: Boolean = false
    private var initialized: Boolean = false

    /**
     * Reads configuration from the manifest without starting the Umeng SDK.
     * Safe to call at process start, before privacy consent is given.
     */
    fun attach(context: Context) {
        val application = context.applicationContext
        val metaData = try {
            application.packageManager
                .getApplicationInfo(application.packageName, PackageManager.GET_META_DATA)
                .metaData ?: Bundle.EMPTY
        } catch (error: Exception) {
            PuzzleLog.e("Analytics", "Unable to read analytics metadata", error)
            Bundle.EMPTY
        }

        appContext = application
        appKey = metaData.getString("UMENG_APPKEY").orEmpty()
        channel = metaData.getString("UMENG_CHANNEL").orEmpty().ifBlank { "official" }
        analyticsEnabled =
            metaData.getString("UMENG_ANALYTICS_ENABLED").orEmpty().toBooleanStrictOrNull() == true
    }

    actual fun initialize() {
        if (initialized) return
        initialized = true

        val application = appContext
        if (application == null || !analyticsEnabled || appKey.isBlank()) {
            PuzzleLog.i("Analytics", "Umeng Android analytics disabled or missing app key")
            return
        }

        UMConfigure.setLogEnabled(false)
        UMConfigure.init(application, appKey, channel, UMConfigure.DEVICE_TYPE_PHONE, null)
        MobclickAgent.setPageCollectionMode(MobclickAgent.PageMode.MANUAL)
        PuzzleLog.i("Analytics", "Umeng Android analytics initialized channel=$channel")
    }

    actual fun trackEvent(name: String, properties: Map<String, String>) {
        val context = appContext
        if (!initialized || !analyticsEnabled || context == null) return
        MobclickAgent.onEventObject(context, name, properties)
    }
}
