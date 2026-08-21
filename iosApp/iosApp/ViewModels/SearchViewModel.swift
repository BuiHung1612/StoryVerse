import Foundation
import SharedLogic

@MainActor
public final class SearchViewModel: ObservableObject {
    @Published public var query: String = ""
    @Published public var selectedCategory: String? = nil
    @Published public private(set) var availableCategories: [String] = [
        "Tiên Hiệp", "Kiếm Hiệp", "Huyền Huyễn", "Đô Thị", "Khoa Huyễn", "Lịch Sử", "Võng Du"
    ]
    @Published public private(set) var results: [Story] = []
    @Published public private(set) var isLoading: Bool = false
    @Published public private(set) var isLoadingMore: Bool = false
    @Published public private(set) var hasNextPage: Bool = false
    @Published public private(set) var currentPage: Int = 1
    @Published public private(set) var errorMessage: String? = nil

    private let storySourceRegistry: StorySourceRegistry
    private var searchTask: Task<Void, Never>?

    public init(storySourceRegistry: StorySourceRegistry = KoinHelper().storySourceRegistry) {
        self.storySourceRegistry = storySourceRegistry
    }

    public func onQueryChange(_ newQuery: String) {
        query = newQuery
        searchTask?.cancel()
        searchTask = Task {
            try? await Task.sleep(nanoseconds: 300_000_000) // 300ms debounce
            if !Task.isCancelled {
                performSearch(query: newQuery, category: selectedCategory, page: 1)
            }
        }
    }

    public func onCategorySelect(_ category: String?) {
        let nextCategory = (selectedCategory == category) ? nil : category
        selectedCategory = nextCategory
        searchTask?.cancel()
        searchTask = Task {
            performSearch(query: query, category: nextCategory, page: 1)
        }
    }

    public func loadNextPage() {
        guard !isLoading && !isLoadingMore && hasNextPage else { return }
        guard let source = storySourceRegistry.getDefaultSource() else { return }

        let nextPage = currentPage + 1
        isLoadingMore = true

        Task {
            do {
                let filter: StoryFilter? = (selectedCategory != nil) ? StoryFilter(category: selectedCategory, status: nil, sort: .popular, author: nil) : nil
                let searchResult = try await source.search(query: query, page: Int32(nextPage), filter: filter)
                if let page = searchResult.getOrNull() as? StoryPage {
                    self.results.append(contentsOf: page.stories)
                    self.currentPage = nextPage
                    self.hasNextPage = page.hasNextPage
                }
                self.isLoadingMore = false
            } catch {
                self.isLoadingMore = false
            }
        }
    }

    public func performSearch(query: String? = nil, category: String? = nil, page: Int = 1) {
        let q = query ?? self.query
        let cat = category ?? self.selectedCategory

        if q.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty && cat == nil {
            results = []
            isLoading = false
            isLoadingMore = false
            hasNextPage = false
            currentPage = 1
            errorMessage = nil
            return
        }

        guard let source = storySourceRegistry.getDefaultSource() else {
            self.errorMessage = "No active story source."
            return
        }

        isLoading = true
        errorMessage = nil
        currentPage = page

        Task {
            do {
                let filter: StoryFilter? = (cat != nil) ? StoryFilter(category: cat, status: nil, sort: .popular, author: nil) : nil
                let searchResult = try await source.search(query: q, page: Int32(page), filter: filter)
                if let page = searchResult.getOrNull() as? StoryPage {
                    self.results = page.stories
                    self.hasNextPage = page.hasNextPage
                } else if let err = searchResult.errorOrNull() {
                    self.errorMessage = err.message
                }
                self.isLoading = false
            } catch {
                self.errorMessage = error.localizedDescription
                self.isLoading = false
            }
        }
    }
}
