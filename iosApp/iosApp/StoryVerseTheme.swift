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
    public let isDark: BooleanLiteralType

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

    public static var current: StoryVerseColors {
        return StoryVerseColors(themeColors: AppTheme.shared.currentColors)
    }
}
