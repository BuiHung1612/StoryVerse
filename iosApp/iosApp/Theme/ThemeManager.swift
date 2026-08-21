import SwiftUI
import SharedLogic

extension Color {
    init(argb: Int64) {
        let alpha = Double((argb >> 24) & 0xFF) / 255.0
        let red   = Double((argb >> 16) & 0xFF) / 255.0
        let green = Double((argb >> 8) & 0xFF) / 255.0
        let blue  = Double(argb & 0xFF) / 255.0
        self.init(.sRGB, red: red, green: green, blue: blue, opacity: alpha)
    }

    init(hex: String) {
        let cleanHex = hex.trimmingCharacters(in: CharacterSet.alphanumerics.inverted)
        var int: UInt64 = 0
        Scanner(string: cleanHex).scanHexInt64(&int)
        let a, r, g, b: UInt64
        switch cleanHex.count {
        case 3: // RGB (12-bit)
            (a, r, g, b) = (255, (int >> 8) * 17, (int >> 4 & 0xF) * 17, (int & 0xF) * 17)
        case 6: // RGB (24-bit)
            (a, r, g, b) = (255, int >> 16, int >> 8 & 0xFF, int & 0xFF)
        case 8: // ARGB (32-bit)
            (a, r, g, b) = (int >> 24, int >> 16 & 0xFF, int >> 8 & 0xFF, int & 0xFF)
        default:
            (a, r, g, b) = (255, 0, 0, 0)
        }
        self.init(
            .sRGB,
            red: Double(r) / 255,
            green: Double(g) / 255,
            blue: Double(b) / 255,
            opacity: Double(a) / 255
        )
    }
}

public struct StoryVerseColors {
    public let primary: Color
    public let onPrimary: Color
    public let primaryContainer: Color
    public let onPrimaryContainer: Color
    public let secondary: Color
    public let background: Color
    public let onBackground: Color
    public let surface: Color
    public let onSurface: Color
    public let surfaceVariant: Color
    public let card: Color
    public let border: Color
    public let textPrimary: Color
    public let textSecondary: Color
    public let textMuted: Color
    public let readerBackground: Color
    public let readerTextColor: Color
    public let accent: Color
    public let isDark: Bool

    public init(themeColors: ThemeColors) {
        self.primary = Color(argb: themeColors.primary)
        self.onPrimary = Color(argb: themeColors.onPrimary)
        self.primaryContainer = Color(argb: themeColors.primaryContainer)
        self.onPrimaryContainer = Color(argb: themeColors.onPrimaryContainer)
        self.secondary = Color(argb: themeColors.secondary)
        self.background = Color(argb: themeColors.background)
        self.onBackground = Color(argb: themeColors.onBackground)
        self.surface = Color(argb: themeColors.surface)
        self.onSurface = Color(argb: themeColors.onSurface)
        self.surfaceVariant = Color(argb: themeColors.surfaceVariant)
        self.card = Color(argb: themeColors.card)
        self.border = Color(argb: themeColors.border)
        self.textPrimary = Color(argb: themeColors.textPrimary)
        self.textSecondary = Color(argb: themeColors.textSecondary)
        self.textMuted = Color(argb: themeColors.textMuted)
        self.readerBackground = Color(argb: themeColors.readerBackground)
        self.readerTextColor = Color(argb: themeColors.readerTextColor)
        self.accent = Color(argb: themeColors.accent)
        self.isDark = themeColors.isDark
    }

    public static var fallback: StoryVerseColors {
        StoryVerseColors(themeColors: AppTheme.shared.getFallbackColors())
    }
}

@MainActor
public final class ThemeManager: ObservableObject {
    public static let shared = ThemeManager()

    @Published public private(set) var colors: StoryVerseColors
    @Published public private(set) var currentPreset: AppThemePreset

    private let themeRepository: ThemeRepository

    public init(themeRepository: ThemeRepository = KoinHelper().themeRepository) {
        self.themeRepository = themeRepository
        let preset = (themeRepository.currentPreset.value as? AppThemePreset) ?? AppThemePreset.light
        let themeColors = (themeRepository.currentColors.value as? ThemeColors) ?? AppTheme.shared.getFallbackColors()
        self.currentPreset = preset
        self.colors = StoryVerseColors(themeColors: themeColors)
    }

    public func selectPreset(_ preset: AppThemePreset) {
        themeRepository.setPreset(preset: preset)
        self.currentPreset = preset
        self.colors = StoryVerseColors(themeColors: themeRepository.getColorsForPreset(preset: preset))
    }

    public func getColors(for preset: AppThemePreset) -> StoryVerseColors {
        StoryVerseColors(themeColors: themeRepository.getColorsForPreset(preset: preset))
    }
}
