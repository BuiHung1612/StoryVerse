import SwiftUI
import SharedLogic

public struct SettingsView: View {
    @EnvironmentObject var themeManager: ThemeManager
    @EnvironmentObject var localizationManager: LocalizationManager
    @StateObject private var viewModel = SettingsViewModel()

    public init() {}

    public var body: some View {
        NavigationStack {
            VStack(spacing: 0) {
                StoryVerseTopBar(
                    title: localizedString(AppStringKey.settingsTitle)
                )

                ScrollView {
                    VStack(spacing: 16) {
                        // Theme Presets Section
                        VStack(alignment: .leading, spacing: 14) {
                            HStack(spacing: 10) {
                                Image(systemName: "paintpalette.fill")
                                    .foregroundColor(themeManager.colors.primary)
                                Text(localizedString(AppStringKey.settingsThemeTitle))
                                    .font(.system(size: 16, weight: .bold))
                                    .foregroundColor(themeManager.colors.textPrimary)
                            }

                            ForEach(viewModel.availableThemes, id: \.id) { preset in
                                let isSelected = themeManager.currentPreset == preset
                                let presetColors = viewModel.getColors(for: preset)
                                let presetName = localizationManager.currentLanguage == .vietnamese ? preset.displayNameVi : preset.displayNameEn

                                Button(action: {
                                    viewModel.selectTheme(preset)
                                }) {
                                    HStack {
                                        // Color swatch
                                        Circle()
                                            .fill(presetColors.background)
                                            .frame(width: 28, height: 28)
                                            .overlay(
                                                Circle()
                                                    .stroke(presetColors.primary, lineWidth: 2)
                                            )
                                            .overlay(
                                                Group {
                                                    if isSelected {
                                                        Image(systemName: "checkmark")
                                                            .font(.system(size: 12, weight: .bold))
                                                            .foregroundColor(presetColors.primary)
                                                    }
                                                }
                                            )

                                        Text(presetName)
                                            .font(.system(size: 14, weight: isSelected ? .bold : .regular))
                                            .foregroundColor(themeManager.colors.textPrimary)
                                            .padding(.leading, 8)

                                        Spacer()

                                        Image(systemName: isSelected ? "largecircle.fill.circle" : "circle")
                                            .foregroundColor(isSelected ? themeManager.colors.primary : themeManager.colors.textMuted)
                                    }
                                    .padding(.vertical, 8)
                                }
                                .buttonStyle(PlainButtonStyle())
                            }
                        }
                        .padding(16)
                        .background(themeManager.colors.card)
                        .cornerRadius(16)
                        .padding(.horizontal, 16)

                        // Language Section
                        VStack(alignment: .leading, spacing: 14) {
                            HStack(spacing: 10) {
                                Image(systemName: "globe")
                                    .foregroundColor(themeManager.colors.primary)
                                Text(localizedString(AppStringKey.settingsLanguageTitle))
                                    .font(.system(size: 16, weight: .bold))
                                    .foregroundColor(themeManager.colors.textPrimary)
                            }

                            ForEach(viewModel.availableLanguages, id: \.code) { language in
                                let isSelected = localizationManager.currentLanguage == language

                                Button(action: {
                                    viewModel.selectLanguage(language)
                                }) {
                                    HStack {
                                        Text("\(language.flagEmoji) \(language.displayName)")
                                            .font(.system(size: 14, weight: isSelected ? .bold : .regular))
                                            .foregroundColor(themeManager.colors.textPrimary)

                                        Spacer()

                                        Image(systemName: isSelected ? "largecircle.fill.circle" : "circle")
                                            .foregroundColor(isSelected ? themeManager.colors.primary : themeManager.colors.textMuted)
                                    }
                                    .padding(.vertical, 8)
                                }
                                .buttonStyle(PlainButtonStyle())
                            }
                        }
                        .padding(16)
                        .background(themeManager.colors.card)
                        .cornerRadius(16)
                        .padding(.horizontal, 16)

                        // Active Story Source Section
                        VStack(alignment: .leading, spacing: 10) {
                            HStack(spacing: 10) {
                                Image(systemName: "folder.fill")
                                    .foregroundColor(themeManager.colors.primary)
                                Text(localizedString(AppStringKey.settingsSectionStorage))
                                    .font(.system(size: 16, weight: .bold))
                                    .foregroundColor(themeManager.colors.textPrimary)
                            }

                            Text(localizedString(AppStringKey.settingsActiveSource, viewModel.activeSourceName))
                                .font(.system(size: 14))
                                .foregroundColor(themeManager.colors.textSecondary)
                        }
                        .frame(maxWidth: .infinity, alignment: .leading)
                        .padding(16)
                        .background(themeManager.colors.card)
                        .cornerRadius(16)
                        .padding(.horizontal, 16)

                        // About Section
                        VStack(alignment: .leading, spacing: 10) {
                            HStack(spacing: 10) {
                                Image(systemName: "info.circle.fill")
                                    .foregroundColor(themeManager.colors.primary)
                                Text(localizedString(AppStringKey.settingsSectionAbout))
                                    .font(.system(size: 16, weight: .bold))
                                    .foregroundColor(themeManager.colors.textPrimary)
                            }

                            Text("StoryVerse Mobile " + localizedString(AppStringKey.settingsVersionFormat, "1.0.0 (KMP + SwiftUI)"))
                                .font(.system(size: 13))
                                .foregroundColor(themeManager.colors.textSecondary)
                        }
                        .frame(maxWidth: .infinity, alignment: .leading)
                        .padding(16)
                        .background(themeManager.colors.card)
                        .cornerRadius(16)
                        .padding(.horizontal, 16)
                    }
                    .padding(.vertical, 16)
                }
            }
            .background(themeManager.colors.background)
        }
    }
}
