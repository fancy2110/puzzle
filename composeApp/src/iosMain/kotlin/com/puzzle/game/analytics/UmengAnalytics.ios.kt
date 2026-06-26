package com.puzzle.game.analytics

private var iosTrackEvent: ((String, Map<String, String>) -> Unit)? = null

fun configureUmengAnalyticsTracker(trackEvent: (String, Map<String, String>) -> Unit) {
    iosTrackEvent = trackEvent
}

actual object PlatformAnalytics {
    actual fun trackEvent(name: String, properties: Map<String, String>) {
        iosTrackEvent?.invoke(name, properties)
    }
}
