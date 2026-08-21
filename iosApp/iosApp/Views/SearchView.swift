import SwiftUI
import SharedLogic

public struct SearchView: View {
    @EnvironmentObject var themeManager: ThemeManager
    @StateObject private var viewModel = SearchViewModel()
    @State private var selectedStoryId: String? = nil

    public init() {}

    public var body: some View {
        NavigationStack {
            VStack(spacing: 0) {
                StoryVerseTopBar(
                    title: localizedString(AppStringKey.tabSearch)
                )

                // Search Bar
                HStack(spacing: 8) {
                    Image(systemName: "magnifyingglass")
                        .foregroundColor(themeManager.colors.textSecondary)

                    TextField(localizedString(AppStringKey.searchHint), text: $viewModel.query)
                        .font(.system(size: 15))
                        .foregroundColor(themeManager.colors.textPrimary)
                        .onChange(of: viewModel.query) { _, newValue in
                            viewModel.onQueryChange(newValue)
                        }

                    if !viewModel.query.isEmpty {
                        Button(action: { viewModel.onQueryChange("") }) {
                            Image(systemName: "xmark.circle.fill")
                                .foregroundColor(themeManager.colors.textMuted)
                        }
                    }
                }
                .padding(.horizontal, 14)
                .padding(.vertical, 10)
                .background(themeManager.colors.card)
                .cornerRadius(10)
                .padding(.horizontal, 16)
                .padding(.vertical, 8)

                // Category Filter Chips
                ScrollView(.horizontal, showsIndicators: false) {
                    HStack(spacing: 8) {
                        ForEach(viewModel.availableCategories, id: \.self) { category in
                            let isSelected = viewModel.selectedCategory == category
                            Button(action: {
                                withAnimation(.spring(response: 0.3, dampingFraction: 0.7)) {
                                    viewModel.onCategorySelect(category)
                                }
                            }) {
                                Text(category)
                                    .font(.system(size: 13, weight: isSelected ? .bold : .medium))
                                    .foregroundColor(isSelected ? themeManager.colors.onPrimary : themeManager.colors.textPrimary)
                                    .padding(.horizontal, 14)
                                    .padding(.vertical, 7)
                                    .background(isSelected ? themeManager.colors.primary : themeManager.colors.card)
                                    .cornerRadius(16)
                                    .overlay(
                                        RoundedRectangle(cornerRadius: 16)
                                            .stroke(isSelected ? Color.clear : themeManager.colors.border, lineWidth: 1)
                                    )
                            }
                        }
                    }
                    .padding(.horizontal, 16)
                    .padding(.vertical, 4)
                }

                // Content
                if viewModel.isLoading {
                    LoadingView()
                } else if let error = viewModel.errorMessage {
                    ErrorView(message: error) {
                        viewModel.performSearch()
                    }
                } else if viewModel.results.isEmpty && (!viewModel.query.isEmpty || viewModel.selectedCategory != nil) {
                    EmptyView(message: localizedString(AppStringKey.searchNoResults), iconName: "magnifyingglass")
                } else if viewModel.results.isEmpty {
                    EmptyView(message: localizedString(AppStringKey.searchHint), iconName: "text.magnifyingglass")
                } else {
                    ScrollView {
                        LazyVStack(spacing: 10) {
                            ForEach(viewModel.results, id: \.id.value) { story in
                                StoryRowItem(story: story) {
                                    selectedStoryId = story.id.value
                                }
                                .padding(.horizontal, 16)
                            }

                            if viewModel.hasNextPage {
                                HStack {
                                    Spacer()
                                    ProgressView()
                                        .tint(themeManager.colors.primary)
                                        .padding(.vertical, 16)
                                    Spacer()
                                }
                                .onAppear {
                                    viewModel.loadNextPage()
                                }
                            }
                        }
                        .padding(.vertical, 10)
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
    }
}
