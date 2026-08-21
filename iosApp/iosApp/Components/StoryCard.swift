import SwiftUI
import SharedLogic

public struct StoryCard: View {
    @EnvironmentObject var themeManager: ThemeManager

    public let story: Story
    public let onClick: () -> Void

    public init(story: Story, onClick: @escaping () -> Void) {
        self.story = story
        self.onClick = onClick
    }

    public var body: some View {
        Button(action: onClick) {
            VStack(alignment: .leading, spacing: 6) {
                // Cover Image
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
                .frame(width: 120, height: 165)
                .clipShape(RoundedRectangle(cornerRadius: 10))
                .shadow(color: Color.black.opacity(0.12), radius: 4, x: 0, y: 2)

                // Title
                Text(story.title)
                    .font(.system(size: 13, weight: .semibold))
                    .foregroundColor(themeManager.colors.textPrimary)
                    .lineLimit(2)
                    .frame(width: 120, alignment: .leading)

                // Author
                Text(story.authorNames)
                    .font(.system(size: 11, weight: .regular))
                    .foregroundColor(themeManager.colors.textSecondary)
                    .lineLimit(1)
                    .frame(width: 120, alignment: .leading)
            }
        }
        .buttonStyle(PlainButtonStyle())
    }

    private var placeholderCover: some View {
        ZStack {
            themeManager.colors.surfaceVariant
            VStack(spacing: 4) {
                Image(systemName: "book.closed.fill")
                    .font(.system(size: 28))
                    .foregroundColor(themeManager.colors.primary.opacity(0.7))
                Text(story.title)
                    .font(.system(size: 10, weight: .medium))
                    .foregroundColor(themeManager.colors.textSecondary)
                    .lineLimit(2)
                    .multilineTextAlignment(.center)
                    .padding(.horizontal, 6)
            }
        }
    }
}
