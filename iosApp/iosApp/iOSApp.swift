import SwiftUI
import SharedLogic

@main
struct iOSApp: App {
    init() {
        KoinKt.doInitKoinIos()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}