import SwiftUI
import SharedLogic

struct ContentView: View {
    @EnvironmentObject var themeManager: ThemeManager

    var body: some View {
        MainTabView()
            .background(themeManager.colors.background.ignoresSafeArea())
    }
}
