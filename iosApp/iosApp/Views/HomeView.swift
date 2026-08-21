import SwiftUI
import SharedLogic

public struct HomeView: View {
    @EnvironmentObject var themeManager: ThemeManager
    @StateObject private var viewModel = HomeViewModel()
    @State private var selectedStoryId: String? = nil

    public init() {}

    public var body: some View {
        NavigationStack {
            VStack(spacing: 0) {
                StoryVerseTopBar(
                    title: localizedString(AppStringKey.appName)
                )

                if viewModel.isLoading && viewModel.latestStories.isEmpty {
                    LoadingView()
                } else if let error = viewModel.errorMessage, viewModel.latestStories.isEmpty {
                    ErrorView(message: error) {
                        viewModel.loadHomeData()
                    }
                } else {
                    ScrollView {
                        LazyVStack(spacing: 20) {
                            // Featured Section
                            if let heroStory = viewModel.featuredStories.first {
                                Button(action: { selectedStoryId = heroStory.id.value }) {
                                    HStack(spacing: 14) {
                                        ZStack {
                                            if let urlStr = heroStory.coverUrl, let url = URL(string: urlStr) {
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
                                        .frame(width: 80, height: 115)
                                        .clipShape(RoundedRectangle(cornerRadius: 10))

                                        VStack(alignment: .leading, spacing: 6) {
                                            Text(heroStory.title)
                                                .font(.system(size: 16, weight: .bold))
                                                .foregroundColor(themeManager.colors.textPrimary)
                                                .lineLimit(2)

                                            Text(heroStory.authorNames)
                                                .font(.system(size: 13))
                                                .foregroundColor(themeManager.colors.textSecondary)
                                                .lineLimit(1)
                                        }

                                        Spacer()

                                        Image(systemName: "chevron.right")
                                            .foregroundColor(themeManager.colors.textMuted)
                                    }
                                    .padding(14)
                                    .background(themeManager.colors.primaryContainer.opacity(0.35))
                                    .cornerRadius(14)
                                }
                                .buttonStyle(PlainButtonStyle())
                                .padding(.horizontal, 16)
                            }

                            // Popular Stories Horizontal Scroll
                            if !viewModel.topRatedStories.isEmpty {
                                VStack(alignment: .leading, spacing: 10) {
                                    Text(localizedString(AppStringKey.sectionPopular))
                                        .font(.system(size: 18, weight: .bold))
                                        .foregroundColor(themeManager.colors.textPrimary)
                                        .padding(.horizontal, 16)

                                    ScrollView(.horizontal, showsIndicators: false) {
                                        LazyHStack(spacing: 12) {
                                            ForEach(viewModel.topRatedStories, id: \.id.value) { story in
                                                StoryCard(story: story) {
                                                    selectedStoryId = story.id.value
                                                }
                                            }
                                        }
                                        .padding(.horizontal, 16)
                                    }
                                }
                            }

                            // Latest Stories Vertical List
                            if !viewModel.latestStories.isEmpty {
                                VStack(alignment: .leading, spacing: 10) {
                                    Text(localizedString(AppStringKey.sectionLatest))
                                        .font(.system(size: 18, weight: .bold))
                                        .foregroundColor(themeManager.colors.textPrimary)
                                        .padding(.horizontal, 16)

                                    LazyVStack(spacing: 10) {
                                        ForEach(viewModel.latestStories, id: \.id.value) { story in
                                            StoryRowItem(story: story) {
                                                selectedStoryId = story.id.value
                                            }
                                            .padding(.horizontal, 16)
                                        }
                                    }
                                }
                            }
                        }
                        .padding(.vertical, 12)
                    }
                    .background(themeManager.colors.background)
                    .refreshable {
                        viewModel.loadHomeData()
                    }
                }
            }
            .background(themeManager.colors.background)
            .navigationDestination(isPresented: Binding(
                get: { selectedStoryId != nil },
                set: { if !$0 { selectedStoryId = nil } }
            )) {
                if let storyId = selectedStoryId {
                    StoryDetailView(storyId: storyId)
                }
            }
        }
        .onAppear {
            if viewModel.latestStories.isEmpty {
                viewModel.loadHomeData()
            }
        }
    }
}
