import Foundation
import SharedLogic

@MainActor
public final class SettingsViewModel: ObservableObject {
    public let availableThemes: [AppThemePreset] = AppThemePreset.entries
    public let availableLanguages: [AppLanguage] = AppLanguage.entries

    @Published public private(set) var activeSourceName: String = "StoryVerse Drive Catalog"

    private let themeRepository: ThemeRepository
    private let localizationRepository: LocalizationRepository
    private let storySourceRegistry: StorySourceRegistry

    public init(
        themeRepository: ThemeRepository = KoinHelper().themeRepository,
        localizationRepository: LocalizationRepository = KoinHelper().localizationRepository,
        storySourceRegistry: StorySourceRegistry = KoinHelper().storySourceRegistry
    ) {
        self.themeRepository = themeRepository
        self.localizationRepository = localizationRepository
        self.storySourceRegistry = storySourceRegistry

        if let defaultSource = storySourceRegistry.getDefaultSource() {
            self.activeSourceName = defaultSource.metadata.name
        }
    }

    public func selectTheme(_ preset: AppThemePreset) {
        ThemeManager.shared.selectPreset(preset)
    }

    public func selectLanguage(_ language: AppLanguage) {
        LocalizationManager.shared.selectLanguage(language)
    }

    public func getColors(for preset: AppThemePreset) -> StoryVerseColors {
        ThemeManager.shared.getColors(for: preset)
    }
}
