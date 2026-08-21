import Foundation
import SharedLogic

@MainActor
public final class StoryDetailViewModel: ObservableObject {
    @Published public private(set) var story: StoryDetail? = nil
    @Published public private(set) var chapters: [Chapter] = []
    @Published public private(set) var isInLibrary: Bool = false
    @Published public private(set) var readingProgress: ReadingProgress? = nil
    @Published public private(set) var isLoading: Bool = false
    @Published public private(set) var errorMessage: String? = nil

    private let storySourceRegistry: StorySourceRegistry
    private let readerRepository: ReaderRepository
    private let localCache: LocalStoryCache

    public init(
        storySourceRegistry: StorySourceRegistry = KoinHelper().storySourceRegistry,
        readerRepository: ReaderRepository = KoinHelper().readerRepository,
        localCache: LocalStoryCache = KoinHelper().localStoryCache
    ) {
        self.storySourceRegistry = storySourceRegistry
        self.readerRepository = readerRepository
        self.localCache = localCache
    }

    public func loadStory(storyId: String) {
        guard let source = storySourceRegistry.getDefaultSource() else {
            self.errorMessage = "No active story source."
            return
        }

        isLoading = true
        errorMessage = nil

        Task {
            do {
                let detailResult = try await source.getStoryDetail(rawId: storyId)
                if let detail = detailResult.getOrNull() as? StoryDetail {
                    self.story = detail
                    // Cache story
                    try? await localCache.cacheStory(story: detail.story, accessedAt: Int64(Date().timeIntervalSince1970 * 1000))
                } else if let err = detailResult.errorOrNull() {
                    self.errorMessage = err.message
                }

                let chaptersResult = try await source.getChapterList(rawId: storyId)
                if let chapterList = chaptersResult.getOrNull() as? [Chapter] {
                    self.chapters = chapterList
                    try? await localCache.cacheChapters(chapters: chapterList)
                }

                self.isLoading = false
            } catch {
                self.errorMessage = error.localizedDescription
                self.isLoading = false
            }
        }
    }

    public func toggleLibrary() {
        self.isInLibrary.toggle()
    }
}
