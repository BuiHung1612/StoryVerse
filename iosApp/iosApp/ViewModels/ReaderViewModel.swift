import Foundation
import SwiftUI
import SharedLogic

@MainActor
public final class ReaderViewModel: ObservableObject {
    @Published public private(set) var currentChapter: ChapterContent? = nil
    @Published public private(set) var chapters: [Chapter] = []
    @Published public var fontSize: CGFloat = 18
    @Published public var showControls: Bool = true
    @Published public private(set) var isLoading: Bool = false
    @Published public private(set) var errorMessage: String? = nil

    private let storySourceRegistry: StorySourceRegistry
    private let localCache: LocalStoryCache
    private let readerRepository: ReaderRepository

    private var currentStoryId: String = ""
    private var currentChapterId: String = ""

    public init(
        storySourceRegistry: StorySourceRegistry = KoinHelper().storySourceRegistry,
        localCache: LocalStoryCache = KoinHelper().localStoryCache,
        readerRepository: ReaderRepository = KoinHelper().readerRepository
    ) {
        self.storySourceRegistry = storySourceRegistry
        self.localCache = localCache
        self.readerRepository = readerRepository
    }

    public func loadChapter(storyId: String, chapterId: String) {
        self.currentStoryId = storyId
        self.currentChapterId = chapterId
        isLoading = true
        errorMessage = nil

        Task {
            // First check local cache
            if let cached = try? await localCache.getCachedChapterContent(chapterId: chapterId) {
                self.currentChapter = cached
                self.isLoading = false
            }

            guard let source = storySourceRegistry.getDefaultSource() else {
                if self.currentChapter == nil {
                    self.errorMessage = "No active story source."
                    self.isLoading = false
                }
                return
            }

            do {
                if self.chapters.isEmpty {
                    let chaptersResult = try await source.getChapterList(rawId: storyId)
                    if let list = chaptersResult.getOrNull() as? [Chapter] {
                        self.chapters = list
                    }
                }

                if self.currentChapter == nil {
                    let contentResult = try await source.getChapterContent(chapterId: chapterId)
                    if let content = contentResult.getOrNull() as? ChapterContent {
                        self.currentChapter = content
                        try? await localCache.cacheChapterContent(content: content, cachedAt: Int64(Date().timeIntervalSince1970 * 1000))
                    } else if let err = contentResult.errorOrNull() {
                        self.errorMessage = err.message
                    }
                }

                self.isLoading = false
            } catch {
                if self.currentChapter == nil {
                    self.errorMessage = error.localizedDescription
                }
                self.isLoading = false
            }
        }
    }

    public func getPrevChapter() -> Chapter? {
        guard let currentIndex = chapters.firstIndex(where: { $0.id == currentChapterId }),
              currentIndex > 0 else { return nil }
        return chapters[currentIndex - 1]
    }

    public func getNextChapter() -> Chapter? {
        guard let currentIndex = chapters.firstIndex(where: { $0.id == currentChapterId }),
              currentIndex + 1 < chapters.count else { return nil }
        return chapters[currentIndex + 1]
    }

    public func increaseFontSize() {
        if fontSize < 32 {
            fontSize += 2
        }
    }

    public func decreaseFontSize() {
        if fontSize > 12 {
            fontSize -= 2
        }
    }

    public func toggleControls() {
        withAnimation(.easeInOut(duration: 0.2)) {
            showControls.toggle()
        }
    }
}
