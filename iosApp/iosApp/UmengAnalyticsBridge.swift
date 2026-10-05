import Foundation
import ComposeApp

#if canImport(UMCommon)
import UMCommon
#endif

enum UmengAnalyticsBridge {
    private static var consentGiven = false
    private static var enabled = false

    /// Registers the Kotlin hooks without starting Umeng.
    /// Called at app launch, before the guardian has consented.
    static func configureTracker() {
        let appKey = Bundle.main.object(forInfoDictionaryKey: "UMENG_APP_KEY") as? String ?? ""
        let enabledValue =
            Bundle.main.object(forInfoDictionaryKey: "UMENG_ANALYTICS_ENABLED") as? String ?? "false"
        enabled = enabledValue.lowercased() == "true" && !appKey.isEmpty

        UmengAnalytics_iosKt.configureUmengAnalytics(
            start: {
                startUmeng()
            },
            trackEvent: { name, properties in
                guard consentGiven, enabled else { return }
                #if canImport(UMCommon)
                MobClick.event(name, attributes: properties)
                #endif
            }
        )
    }

    /// Starts the Umeng SDK. Invoked from shared code only after consent.
    private static func startUmeng() {
        guard !consentGiven else { return }
        consentGiven = true

        let appKey = Bundle.main.object(forInfoDictionaryKey: "UMENG_APP_KEY") as? String ?? ""
        let channel = Bundle.main.object(forInfoDictionaryKey: "UMENG_CHANNEL") as? String
            ?? "app_store"

        #if canImport(UMCommon)
        if enabled {
            UMConfigure.initWithAppkey(appKey, channel: channel)
            #if DEBUG
            UMConfigure.setLogEnabled(true)
            #endif
        }
        #endif
    }
}
