import SwiftUI
import SharedLogic

@main
struct iOSApp: App {
    @StateObject private var themeManager = ThemeManager.shared
    @StateObject private var localizationManager = LocalizationManager.shared

    init() {
        KoinKt.doInitKoinIos()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
                .environmentObject(themeManager)
                .environmentObject(localizationManager)
                .preferredColorScheme(themeManager.colors.isDark ? .dark : .light)
        }
    }
}
