import Foundation
import SharedLogic

@MainActor
public final class SearchViewModel: ObservableObject {
    @Published public var query: String = ""
    @Published public private(set) var results: [Story] = []
    @Published public private(set) var isLoading: Bool = false
    @Published public private(set) var errorMessage: String? = nil

    private let storySourceRegistry: StorySourceRegistry
    private var searchTask: Task<Void, Never>?

    public init(storySourceRegistry: StorySourceRegistry = KoinHelper().storySourceRegistry) {
        self.storySourceRegistry = storySourceRegistry
    }

    public func onQueryChange(_ newQuery: String) {
        query = newQuery
        searchTask?.cancel()

        guard !newQuery.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty else {
            results = []
            isLoading = false
            errorMessage = nil
            return
        }

        searchTask = Task {
            try? await Task.sleep(nanoseconds: 300_000_000) // 300ms debounce
            if !Task.isCancelled {
                performSearch(newQuery)
            }
        }
    }

    public func performSearch(_ queryToSearch: String) {
        guard let source = storySourceRegistry.getDefaultSource() else {
            self.errorMessage = "No active story source."
            return
        }

        isLoading = true
        errorMessage = nil

        Task {
            do {
                let searchResult = try await source.search(query: queryToSearch, page: 1, filter: nil)
                if let page = searchResult.getOrNull() as? StoryPage {
                    self.results = page.stories
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
