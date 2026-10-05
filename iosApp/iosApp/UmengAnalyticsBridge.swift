import Foundation
import ComposeApp

#if canImport(UMCommon)
import UMCommon
#endif

enum UmengAnalyticsBridge {
    static func configure() {
        let appKey = Bundle.main.object(forInfoDictionaryKey: "UMENG_APP_KEY") as? String ?? ""
        let channel = Bundle.main.object(forInfoDictionaryKey: "UMENG_CHANNEL") as? String ?? "app_store"
        let enabledValue = Bundle.main.object(forInfoDictionaryKey: "UMENG_ANALYTICS_ENABLED") as? String ?? "false"
        let enabled = enabledValue.lowercased() == "true" && !appKey.isEmpty

        #if canImport(UMCommon)
        if enabled {
            UMConfigure.initWithAppkey(appKey, channel: channel)
            #if DEBUG
            UMConfigure.setLogEnabled(true)
            #endif
        }
        #endif

        UmengAnalytics_iosKt.configureUmengAnalyticsTracker { name, properties in
            guard enabled else { return }
            #if canImport(UMCommon)
            MobClick.event(name, attributes: properties)
            #endif
        }
    }
}
