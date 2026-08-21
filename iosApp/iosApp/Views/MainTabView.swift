import SwiftUI
import SharedLogic

public struct MainTabView: View {
    @EnvironmentObject var themeManager: ThemeManager
    @EnvironmentObject var localizationManager: LocalizationManager
    @State private var selectedTab: Int = 0

    public init() {}

    public var body: some View {
        TabView(selection: $selectedTab) {
            HomeView()
                .tabItem {
                    Label(
                        localizedString(AppStringKey.tabDiscover),
                        systemImage: "safari"
                    )
                }
                .tag(0)

            SearchView()
                .tabItem {
                    Label(
                        localizedString(AppStringKey.tabSearch),
                        systemImage: "magnifyingglass"
                    )
                }
                .tag(1)

            LibraryView()
                .tabItem {
                    Label(
                        localizedString(AppStringKey.tabLibrary),
                        systemImage: "books.vertical"
                    )
                }
                .tag(2)

            SettingsView()
                .tabItem {
                    Label(
                        localizedString(AppStringKey.tabSettings),
                        systemImage: "gearshape"
                    )
                }
                .tag(3)
        }
        .tint(themeManager.colors.primary)
        .background(themeManager.colors.background)
    }
}
