package com.puzzle.game.analytics

private var iosStart: (() -> Unit)? = null
private var iosTrackEvent: ((String, Map<String, String>) -> Unit)? = null

/** Registers the platform hooks. Called by the Swift bridge at app launch. */
fun configureUmengAnalytics(
    start: (() -> Unit)? = null,
    trackEvent: ((String, Map<String, String>) -> Unit)? = null
) {
    start?.let { iosStart = it }
    trackEvent?.let { iosTrackEvent = it }
}

actual object PlatformAnalytics {
    actual fun initialize() {
        iosStart?.invoke()
    }

    actual fun trackEvent(name: String, properties: Map<String, String>) {
        iosTrackEvent?.invoke(name, properties)
    }
}
