import SwiftUI
import SharedLogic

public struct LoadingView: View {
    @EnvironmentObject var themeManager: ThemeManager

    public init() {}

    public var body: some View {
        VStack(spacing: 12) {
            ProgressView()
                .scaleEffect(1.3)
                .tint(themeManager.colors.primary)
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(themeManager.colors.background)
    }
}

public struct ErrorView: View {
    @EnvironmentObject var themeManager: ThemeManager

    public let message: String
    public let onRetry: () -> Void

    public init(message: String, onRetry: @escaping () -> Void) {
        self.message = message
        self.onRetry = onRetry
    }

    public var body: some View {
        VStack(spacing: 16) {
            Image(systemName: "exclamationmark.triangle.fill")
                .font(.system(size: 48))
                .foregroundColor(.orange)

            Text(message)
                .font(.system(size: 15, weight: .medium))
                .foregroundColor(themeManager.colors.textPrimary)
                .multilineTextAlignment(.center)
                .padding(.horizontal, 32)

            Button(action: onRetry) {
                Text(localizedString(AppStringKey.actionRetry))
                    .font(.system(size: 14, weight: .semibold))
                    .foregroundColor(themeManager.colors.onPrimary)
                    .padding(.horizontal, 24)
                    .padding(.vertical, 10)
                    .background(themeManager.colors.primary)
                    .cornerRadius(8)
            }
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(themeManager.colors.background)
    }
}

public struct EmptyView: View {
    @EnvironmentObject var themeManager: ThemeManager

    public let message: String
    public let iconName: String

    public init(message: String, iconName: String = "tray") {
        self.message = message
        self.iconName = iconName
    }

    public var body: some View {
        VStack(spacing: 12) {
            Image(systemName: iconName)
                .font(.system(size: 48))
                .foregroundColor(themeManager.colors.textMuted)

            Text(message)
                .font(.system(size: 15, weight: .medium))
                .foregroundColor(themeManager.colors.textSecondary)
                .multilineTextAlignment(.center)
                .padding(.horizontal, 32)
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(themeManager.colors.background)
    }
}
