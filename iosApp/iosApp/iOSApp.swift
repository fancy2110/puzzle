import SwiftUI
import ComposeApp

@main
struct iOSApp: App {
    init() {
        UmengAnalyticsBridge.configure()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
