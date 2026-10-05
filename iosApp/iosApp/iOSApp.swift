import SwiftUI
import ComposeApp

@main
struct iOSApp: App {
    init() {
        UmengAnalyticsBridge.configureTracker()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
