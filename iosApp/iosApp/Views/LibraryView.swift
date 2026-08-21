import SwiftUI
import SharedLogic

public struct LibraryView: View {
    @EnvironmentObject var themeManager: ThemeManager
    @StateObject private var viewModel = LibraryViewModel()
    @State private var selectedStoryId: String? = nil
    @Namespace private var tabAnimation

    public init() {}

    public var body: some View {
        NavigationStack {
            VStack(spacing: 0) {
                StoryVerseTopBar(
                    title: localizedString(AppStringKey.libraryTitle),
                    actions: {
                        if viewModel.selectedTab == .history && !viewModel.historyEntries.isEmpty {
                            Button(action: { viewModel.clearHistory() }) {
                                Image(systemName: "trash")
                                    .foregroundColor(themeManager.colors.textSecondary)
                            }
                        }
                    }
                )

                // Tab Selector with smooth sliding indicator
                HStack(spacing: 0) {
                    ForEach(LibraryTab.allCases) { tab in
                        Button(action: {
                            withAnimation(.spring(response: 0.35, dampingFraction: 0.75)) {
                                viewModel.selectedTab = tab
                            }
                        }) {
                            VStack(spacing: 6) {
                                Text(tab.title)
                                    .font(.system(size: 14, weight: viewModel.selectedTab == tab ? .bold : .medium))
                                    .foregroundColor(viewModel.selectedTab == tab ? themeManager.colors.primary : themeManager.colors.textSecondary)

                                ZStack {
                                    if viewModel.selectedTab == tab {
                                        RoundedRectangle(cornerRadius: 1.5)
                                            .fill(themeManager.colors.primary)
                                            .frame(height: 3)
                                            .matchedGeometryEffect(id: "LIBRARY_ACTIVE_TAB_INDICATOR", in: tabAnimation)
                                    } else {
                                        Color.clear
                                            .frame(height: 3)
                                    }
                                }
                            }
                        }
                        .frame(maxWidth: .infinity)
                    }
                }
                .padding(.top, 8)
                .background(themeManager.colors.background)

                // Swipeable Tab Content
                if viewModel.isLoading && viewModel.savedStories.isEmpty {
                    LoadingView()
                } else {
                    TabView(selection: $viewModel.selectedTab.animation(.spring(response: 0.35, dampingFraction: 0.75))) {
                        // 1. Saved Stories Tab
                        Group {
                            if viewModel.savedStories.isEmpty {
                                EmptyView(message: localizedString(AppStringKey.libraryEmptySubtitle), iconName: "bookmark")
                            } else {
                                ScrollView {
                                    LazyVStack(spacing: 10) {
                                        ForEach(viewModel.savedStories, id: \.id.value) { story in
                                            StoryRowItem(story: story) {
                                                selectedStoryId = story.id.value
                                            }
                                            .padding(.horizontal, 16)
                                        }
                                    }
                                    .padding(.vertical, 12)
                                }
                            }
                        }
                        .tag(LibraryTab.saved)

                        // 2. History Tab
                        Group {
                            if viewModel.historyEntries.isEmpty {
                                EmptyView(message: localizedString(AppStringKey.libraryEmptySubtitle), iconName: "clock")
                            } else {
                                ScrollView {
                                    LazyVStack(spacing: 10) {
                                        ForEach(viewModel.historyEntries, id: \.story.id.value) { entry in
                                            StoryRowItem(story: entry.story) {
                                                selectedStoryId = entry.story.id.value
                                            }
                                            .padding(.horizontal, 16)
                                        }
                                    }
                                    .padding(.vertical, 12)
                                }
                            }
                        }
                        .tag(LibraryTab.history)

                        // 3. Downloads Tab
                        EmptyView(message: localizedString(AppStringKey.libraryEmptySubtitle), iconName: "arrow.down.circle")
                            .tag(LibraryTab.downloads)
                    }
                    .tabViewStyle(.page(indexDisplayMode: .never))
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
            viewModel.loadLibrary()
        }
    }
}
