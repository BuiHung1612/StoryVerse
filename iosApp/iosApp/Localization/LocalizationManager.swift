import Foundation
import SharedLogic

@MainActor
public final class LocalizationManager: ObservableObject {
    public static let shared = LocalizationManager()

    @Published public private(set) var currentLanguage: AppLanguage

    private let localizationRepository: LocalizationRepository

    public init(localizationRepository: LocalizationRepository = KoinHelper().localizationRepository) {
        self.localizationRepository = localizationRepository
        let lang = (localizationRepository.currentLanguage.value as? AppLanguage) ?? AppLanguage.vietnamese
        self.currentLanguage = lang
    }

    public func selectLanguage(_ language: AppLanguage) {
        localizationRepository.setLanguage(language: language)
        self.currentLanguage = language
    }

    public func string(for key: AppStringKey) -> String {
        IosStringsKt.getLocalizedString(key: key)
    }

    public func string(for key: AppStringKey, _ arg: String) -> String {
        IosStringsKt.getLocalizedString(key: key, arg: arg)
    }
}

public func localizedString(_ key: AppStringKey) -> String {
    IosStringsKt.getLocalizedString(key: key)
}

public func localizedString(_ key: AppStringKey, _ arg: String) -> String {
    IosStringsKt.getLocalizedString(key: key, arg: arg)
}
