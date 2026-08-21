import SwiftUI
import SharedLogic

public struct StoryRowItem: View {
    @EnvironmentObject var themeManager: ThemeManager

    public let story: Story
    public let onClick: () -> Void

    public init(story: Story, onClick: @escaping () -> Void) {
        self.story = story
        self.onClick = onClick
    }

    public var body: some View {
        Button(action: onClick) {
            HStack(spacing: 12) {
                // Cover
                ZStack {
                    if let urlStr = story.coverUrl, let url = URL(string: urlStr) {
                        AsyncImage(url: url) { phase in
                            switch phase {
                            case .success(let image):
                                image
                                    .resizable()
                                    .aspectRatio(contentMode: .fill)
                            case .failure(_), .empty:
                                placeholderCover
                            @unknown default:
                                placeholderCover
                            }
                        }
                    } else {
                        placeholderCover
                    }
                }
                .frame(width: 65, height: 90)
                .clipShape(RoundedRectangle(cornerRadius: 8))

                // Info
                VStack(alignment: .leading, spacing: 4) {
                    Text(story.title)
                        .font(.system(size: 15, weight: .bold))
                        .foregroundColor(themeManager.colors.textPrimary)
                        .lineLimit(2)

                    Text(story.authorNames)
                        .font(.system(size: 13, weight: .regular))
                        .foregroundColor(themeManager.colors.textSecondary)
                        .lineLimit(1)

                    if !story.categories.isEmpty {
                        HStack(spacing: 6) {
                            ForEach(Array(story.categories.prefix(2)), id: \.id) { category in
                                Text(category.name)
                                    .font(.system(size: 10, weight: .medium))
                                    .foregroundColor(themeManager.colors.primary)
                                    .padding(.horizontal, 6)
                                    .padding(.vertical, 2)
                                    .background(themeManager.colors.primaryContainer.opacity(0.4))
                                    .cornerRadius(4)
                            }
                        }
                    }
                }

                Spacer()

                Image(systemName: "chevron.right")
                    .font(.system(size: 13, weight: .semibold))
                    .foregroundColor(themeManager.colors.textMuted)
            }
            .padding(10)
            .background(themeManager.colors.card)
            .cornerRadius(12)
            .overlay(
                RoundedRectangle(cornerRadius: 12)
                    .stroke(themeManager.colors.border.opacity(0.5), lineWidth: 1)
            )
        }
        .buttonStyle(PlainButtonStyle())
    }

    private var placeholderCover: some View {
        ZStack {
            themeManager.colors.surfaceVariant
            Image(systemName: "book.closed.fill")
                .font(.system(size: 22))
                .foregroundColor(themeManager.colors.primary.opacity(0.7))
        }
    }
}
