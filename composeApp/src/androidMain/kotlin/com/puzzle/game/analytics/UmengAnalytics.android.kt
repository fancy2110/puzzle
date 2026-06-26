package com.puzzle.game.analytics

import android.content.Context
import android.os.Bundle
import com.puzzle.logger.PuzzleLog
import com.umeng.analytics.MobclickAgent
import com.umeng.commonsdk.UMConfigure

actual object PlatformAnalytics {
    private var appContext: Context? = null
    private var enabled: Boolean = false

    fun initialize(context: Context) {
        val application = context.applicationContext
        val metaData = application.packageManager
            .getApplicationInfo(application.packageName, android.content.pm.PackageManager.GET_META_DATA)
            .metaData ?: Bundle.EMPTY
        val appKey = metaData.getString("UMENG_APPKEY").orEmpty()
        val channel = metaData.getString("UMENG_CHANNEL").orEmpty().ifBlank { "official" }
        val shouldEnable = metaData.getString("UMENG_ANALYTICS_ENABLED").orEmpty().toBooleanStrictOrNull() == true

        appContext = application
        enabled = shouldEnable && appKey.isNotBlank()
        if (!enabled) {
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
        if (!enabled || context == null) return
        MobclickAgent.onEventObject(context, name, properties)
    }
}
