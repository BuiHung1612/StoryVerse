import SwiftUI
import SharedLogic

public struct StoryDetailView: View {
    @EnvironmentObject var themeManager: ThemeManager
    @Environment(\.dismiss) private var dismiss
    @StateObject private var viewModel = StoryDetailViewModel()

    public let storyId: String

    @State private var selectedChapterId: String? = nil

    public init(storyId: String) {
        self.storyId = storyId
    }

    public var body: some View {
        VStack(spacing: 0) {
            StoryVerseTopBar(
                title: viewModel.story?.story.title ?? localizedString(AppStringKey.appName),
                canNavigateBack: true,
                onNavigateBack: { dismiss() },
                actions: {
                    Button(action: { viewModel.toggleLibrary() }) {
                        Image(systemName: viewModel.isInLibrary ? "bookmark.fill" : "bookmark")
                            .font(.system(size: 18))
                            .foregroundColor(viewModel.isInLibrary ? themeManager.colors.primary : themeManager.colors.textSecondary)
                    }
                }
            )

            if viewModel.isLoading && viewModel.story == nil {
                LoadingView()
            } else if let error = viewModel.errorMessage, viewModel.story == nil {
                ErrorView(message: error) {
                    viewModel.loadStory(storyId: storyId)
                }
            } else if let detail = viewModel.story {
                ScrollView {
                    VStack(alignment: .leading, spacing: 16) {
                        // Header info
                        HStack(alignment: .top, spacing: 14) {
                            ZStack {
                                if let urlStr = detail.story.coverUrl, let url = URL(string: urlStr) {
                                    AsyncImage(url: url) { phase in
                                        if let image = phase.image {
                                            image.resizable().aspectRatio(contentMode: .fill)
                                        } else {
                                            themeManager.colors.surfaceVariant
                                        }
                                    }
                                } else {
                                    themeManager.colors.surfaceVariant
                                }
                            }
                            .frame(width: 100, height: 145)
                            .clipShape(RoundedRectangle(cornerRadius: 10))

                            VStack(alignment: .leading, spacing: 6) {
                                Text(detail.story.title)
                                    .font(.system(size: 18, weight: .bold))
                                    .foregroundColor(themeManager.colors.textPrimary)
                                    .lineLimit(3)

                                Text(detail.story.authorNames)
                                    .font(.system(size: 14))
                                    .foregroundColor(themeManager.colors.textSecondary)

                                if !detail.story.categories.isEmpty {
                                    FlowLayout(spacing: 6) {
                                        ForEach(detail.story.categories, id: \.id) { category in
                                            Text(category.name)
                                                .font(.system(size: 11, weight: .medium))
                                                .foregroundColor(themeManager.colors.primary)
                                                .padding(.horizontal, 8)
                                                .padding(.vertical, 3)
                                                .background(themeManager.colors.primaryContainer.opacity(0.35))
                                                .cornerRadius(6)
                                        }
                                    }
                                }
                            }
                        }
                        .padding(.horizontal, 16)

                        // Description
                        if let desc = detail.story.description_, !desc.isEmpty {
                            VStack(alignment: .leading, spacing: 8) {
                                Text(localizedString(AppStringKey.detailDescriptionTitle))
                                    .font(.system(size: 16, weight: .bold))
                                    .foregroundColor(themeManager.colors.textPrimary)

                                Text(desc)
                                    .font(.system(size: 14))
                                    .foregroundColor(themeManager.colors.textSecondary)
                                    .lineSpacing(4)
                            }
                            .padding(14)
                            .background(themeManager.colors.card)
                            .cornerRadius(12)
                            .padding(.horizontal, 16)
                        }

                        // Chapters Header
                        HStack {
                            Text(localizedString(AppStringKey.detailChapterListTitle))
                                .font(.system(size: 17, weight: .bold))
                                .foregroundColor(themeManager.colors.textPrimary)

                            Spacer()

                            Text("\(viewModel.chapters.count) chương")
                                .font(.system(size: 13))
                                .foregroundColor(themeManager.colors.textSecondary)
                        }
                        .padding(.horizontal, 16)
                        .padding(.top, 8)

                        // Chapter items
                        LazyVStack(spacing: 8) {
                            ForEach(viewModel.chapters, id: \.id) { chapter in
                                Button(action: { selectedChapterId = chapter.id }) {
                                    HStack {
                                        Text(chapter.title)
                                            .font(.system(size: 14))
                                            .foregroundColor(themeManager.colors.textPrimary)
                                            .lineLimit(1)

                                        Spacer()

                                        Image(systemName: "chevron.right")
                                            .font(.system(size: 12))
                                            .foregroundColor(themeManager.colors.textMuted)
                                    }
                                    .padding(12)
                                    .background(themeManager.colors.card)
                                    .cornerRadius(8)
                                }
                                .buttonStyle(PlainButtonStyle())
                                .padding(.horizontal, 16)
                            }
                        }
                    }
                    .padding(.vertical, 12)
                }
            }
        }
        .background(themeManager.colors.background)
        .navigationBarBackButtonHidden(true)
        .navigationDestination(isPresented: Binding(
            get: { selectedChapterId != nil },
            set: { if !$0 { selectedChapterId = nil } }
        )) {
            if let chapterId = selectedChapterId {
                ReaderView(storyId: storyId, chapterId: chapterId)
            }
        }
        .onAppear {
            viewModel.loadStory(storyId: storyId)
        }
    }
}

// Simple Flow layout for tags
struct FlowLayout: Layout {
    var spacing: CGFloat = 6

    func sizeThatFits(proposal: ProposedViewSize, subviews: Subviews, cache: inout ()) -> CGSize {
        let width = proposal.width ?? .infinity
        var currentX: CGFloat = 0
        var currentY: CGFloat = 0
        var rowHeight: CGFloat = 0

        for view in subviews {
            let size = view.sizeThatFits(.unspecified)
            if currentX + size.width > width && currentX > 0 {
                currentX = 0
                currentY += rowHeight + spacing
                rowHeight = 0
            }
            currentX += size.width + spacing
            rowHeight = max(rowHeight, size.height)
        }
        return CGSize(width: width, height: currentY + rowHeight)
    }

    func placeSubviews(in bounds: CGRect, proposal: ProposedViewSize, subviews: Subviews, cache: inout ()) {
        var currentX = bounds.minX
        var currentY = bounds.minY
        var rowHeight: CGFloat = 0

        for view in subviews {
            let size = view.sizeThatFits(.unspecified)
            if currentX + size.width > bounds.maxX && currentX > bounds.minX {
                currentX = bounds.minX
                currentY += rowHeight + spacing
                rowHeight = 0
            }
            view.place(at: CGPoint(x: currentX, y: currentY), proposal: ProposedViewSize(size))
            currentX += size.width + spacing
            rowHeight = max(rowHeight, size.height)
        }
    }
}
