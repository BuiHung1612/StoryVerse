import Foundation
import SharedLogic

@MainActor
public final class HomeViewModel: ObservableObject {
    @Published public private(set) var featuredStories: [Story] = []
    @Published public private(set) var topRatedStories: [Story] = []
    @Published public private(set) var latestStories: [Story] = []
    @Published public private(set) var isLoading: Bool = false
    @Published public private(set) var errorMessage: String? = nil

    private let storySourceRegistry: StorySourceRegistry

    public init(storySourceRegistry: StorySourceRegistry = KoinHelper().storySourceRegistry) {
        self.storySourceRegistry = storySourceRegistry
    }

    public func loadHomeData() {
        guard let source = storySourceRegistry.getDefaultSource() else {
            self.errorMessage = "No active story source found."
            return
        }

        isLoading = true
        errorMessage = nil

        Task {
            do {
                // Fetch popular / featured
                let popularResult = try await source.getPopular(page: 1)
                if let page = popularResult.getOrNull() as? StoryPage {
                    let stories = page.stories
                    self.featuredStories = Array(stories.prefix(5))
                    self.topRatedStories = Array(stories.dropFirst(0).prefix(10))
                }

                // Fetch latest updates
                let latestResult = try await source.getLatestUpdates(page: 1)
                if let page = latestResult.getOrNull() as? StoryPage {
                    self.latestStories = page.stories
                }

                self.isLoading = false
            } catch {
                self.errorMessage = error.localizedDescription
                self.isLoading = false
            }
        }
    }
}
