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
    @Published public private(set) var bookmarks: [Bookmark] = []
    
    // Preferences
    @Published public var fontSize: CGFloat = 17
    @Published public var fontFamily: ReaderFontFamily = .system
    @Published public var lineSpacingMultiplier: CGFloat = 1.55
    @Published public var themePreset: ReaderThemePreset = .default_
    @Published public var horizontalPadding: CGFloat = 20
    
    // Sheet States
    @Published public var showTocSheet: Bool = false
    @Published public var showSettingsSheet: Bool = false
    @Published public var showBookmarksSheet: Bool = false
    @Published public var showControls: Bool = true
    
    @Published public private(set) var isLoading: Bool = false
    @Published public private(set) var errorMessage: String? = nil

    private let readerUseCase: ReaderUseCase
    private var currentStoryId: String = ""
    private var currentChapterId: String = ""
    private var progressTask: Task<Void, Never>? = nil

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

    public func deleteBookmark(id: String) {
        Task {
            _ = try? await readerUseCase.deleteBookmark(bookmarkId: id)
            bookmarks.removeAll(where: { $0.id == id })
        }
    }

    public func onScrollPositionChanged(scrollOffset: Int32, progress: Float) {
        guard !currentStoryId.isEmpty, let currentChapter = chapters.first(where: { $0.id == currentChapterId }) else { return }
        let parsedStoryId = StoryId.companion.from(compositeValue: currentStoryId)

        progressTask?.cancel()
        progressTask = Task {
            try? await Task.sleep(nanoseconds: 500_000_000) // 500ms debounce
            _ = try? await readerUseCase.saveScrollPosition(
                storyId: parsedStoryId,
                chapterId: currentChapter.id,
                chapterIndex: currentChapter.index,
                scrollOffset: scrollOffset,
                progressPercentage: progress
            )
        }
    }

    public func toggleTocSheet() {
        showTocSheet.toggle()
        if showTocSheet {
            showSettingsSheet = false
            showBookmarksSheet = false
        }
    }

    public func toggleSettingsSheet() {
        showSettingsSheet.toggle()
        if showSettingsSheet {
            showTocSheet = false
            showBookmarksSheet = false
        }
    }

    public func toggleBookmarksSheet() {
        showBookmarksSheet.toggle()
        if showBookmarksSheet {
            showTocSheet = false
            showSettingsSheet = false
        }
    }

    public func increaseFontSize() {
        if fontSize < 32 {
            fontSize += 1.5
            readerUseCase.setFontSize(size: Float(fontSize))
        }
    }

    public func decreaseFontSize() {
        if fontSize > 12 {
            fontSize -= 1.5
            readerUseCase.setFontSize(size: Float(fontSize))
        }
    }

    public func setFontSize(_ size: CGFloat) {
        self.fontSize = max(12, min(32, size))
        readerUseCase.setFontSize(size: Float(self.fontSize))
    }

    public func setFontFamily(_ family: ReaderFontFamily) {
        self.fontFamily = family
        readerUseCase.setFontFamily(fontFamily: family)
    }

    public func setLineSpacing(_ multiplier: CGFloat) {
        self.lineSpacingMultiplier = multiplier
        readerUseCase.setLineSpacing(multiplier: Float(multiplier))
    }

    public func setThemePreset(_ preset: ReaderThemePreset) {
        self.themePreset = preset
        readerUseCase.setThemePreset(preset: preset)
    }

    public func toggleControls() {
        withAnimation(.easeInOut(duration: 0.2)) {
            showControls.toggle()
        }
    }
}
