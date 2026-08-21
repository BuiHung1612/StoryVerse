import SwiftUI
import SharedLogic

public struct StoryVerseTopBar<Actions: View>: View {
    @EnvironmentObject var themeManager: ThemeManager

    public let title: String
    public let canNavigateBack: Bool
    public let onNavigateBack: (() -> Void)?
    public let actions: Actions

    public init(
        title: String,
        canNavigateBack: Bool = false,
        onNavigateBack: (() -> Void)? = nil,
        @ViewBuilder actions: () -> Actions = { SwiftUI.EmptyView() }
    ) {
        self.title = title
        self.canNavigateBack = canNavigateBack
        self.onNavigateBack = onNavigateBack
        self.actions = actions()
    }

    public var body: some View {
        HStack(spacing: 12) {
            if canNavigateBack {
                Button(action: {
                    onNavigateBack?()
                }) {
                    Image(systemName: "arrow.backward")
                        .font(.system(size: 18, weight: .semibold))
                        .foregroundColor(themeManager.colors.onBackground)
                        .frame(width: 36, height: 36)
                }
            }

            Text(title)
                .font(.system(size: 19, weight: .bold))
                .foregroundColor(themeManager.colors.onBackground)
                .lineLimit(1)

            Spacer()

            actions
        }
        .padding(.horizontal, 16)
        .frame(height: 52)
        .background(themeManager.colors.background)
    }
}
