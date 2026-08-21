import Foundation
import SwiftUI
import SharedLogic

@MainActor
public final class ReaderViewModel: ObservableObject {
    @Published public private(set) var currentChapter: ChapterContent? = nil
    @Published public private(set) var story: Story? = nil
    @Published public private(set) var chapters: [Chapter] = []
    @Published public private(set) var prevChapter: Chapter? = nil
    @Published public private(set) var nextChapter: Chapter? = nil
    @Published public private(set) var wordCount: Int32 = 0
    @Published public private(set) var estimatedReadMinutes: Int32 = 1
    @Published public private(set) var isBookmarked: Bool = false
    @Published public var showTocSheet: Bool = false
    @Published public var fontSize: CGFloat = 18
    @Published public var showControls: Bool = true
    @Published public private(set) var isLoading: Bool = false
    @Published public private(set) var errorMessage: String? = nil

    private let readerUseCase: ReaderUseCase

    private var currentStoryId: String = ""
    private var currentChapterId: String = ""

    public init(
        readerUseCase: ReaderUseCase = KoinHelper().readerUseCase
    ) {
        self.readerUseCase = readerUseCase
    }

    public func loadChapter(storyId: String, chapterId: String) {
        self.currentStoryId = storyId
        self.currentChapterId = chapterId
        self.isLoading = true
        self.errorMessage = nil

        Task {
            do {
                let parsedStoryId = StoryId.companion.from(compositeValue: storyId)
                let sessionResult = try await readerUseCase.loadChapterSession(
                    storyId: parsedStoryId,
                    chapterId: chapterId,
                    prefetchScope: nil
                )

                if let session = sessionResult.getOrNull() as? ReaderSessionData {
                    self.story = session.story
                    self.currentChapter = session.content
                    self.chapters = session.chapters
                    self.prevChapter = session.prevChapter
                    self.nextChapter = session.nextChapter
                    self.wordCount = session.wordCount
                    self.estimatedReadMinutes = session.estimatedReadMinutes
                    self.errorMessage = nil
                } else if let err = sessionResult.errorOrNull() {
                    self.errorMessage = err.message
                }
                self.isLoading = false
            } catch {
                self.errorMessage = error.localizedDescription
                self.isLoading = false
            }
        }
    }

    public func toggleBookmark() {
        guard let story = story, let currentChapter = chapters.first(where: { $0.id == currentChapterId }) else { return }

        Task {
            do {
                let result = try await readerUseCase.toggleBookmark(story: story, chapter: currentChapter, snippet: nil)
                if let bookmarked = result.getOrNull() as? NSNumber {
                    self.isBookmarked = bookmarked.boolValue
                }
            } catch {
                // Ignore error
            }
        }
    }

    public func toggleTocSheet() {
        showTocSheet.toggle()
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
