import Foundation
import SharedLogic

@MainActor
public final class StoryDetailViewModel: ObservableObject {
    @Published public private(set) var story: StoryDetail? = nil
    @Published public private(set) var chapters: [Chapter] = []
    @Published public private(set) var isInLibrary: Bool = false
    @Published public private(set) var isDownloaded: Bool = false
    @Published public private(set) var isDownloading: Bool = false
    @Published public private(set) var downloadProgress: Float = 0
    @Published public private(set) var readingProgress: ReadingProgress? = nil
    @Published public private(set) var isLoading: Bool = false
    @Published public private(set) var errorMessage: String? = nil
    @Published public var downloadErrorMessage: String? = nil
    @Published public var downloadSuccessMessage: String? = nil

    private let storySourceRegistry: StorySourceRegistry
    private let readerRepository: ReaderRepository
    private let localCache: LocalStoryCache
    private let downloadManager: DownloadManager

    public init(
        storySourceRegistry: StorySourceRegistry = KoinHelper().storySourceRegistry,
        readerRepository: ReaderRepository = KoinHelper().readerRepository,
        localCache: LocalStoryCache = KoinHelper().localStoryCache,
        downloadManager: DownloadManager = KoinHelper().downloadManager
    ) {
        self.storySourceRegistry = storySourceRegistry
        self.readerRepository = readerRepository
        self.localCache = localCache
        self.downloadManager = downloadManager
    }

    public func loadStory(storyId: String) {
        let parsedStoryId = StoryId.companion.from(compositeValue: storyId)
        let source = storySourceRegistry.getSource(sourceId: parsedStoryId.sourceId) ?? storySourceRegistry.getDefaultSource()
        guard let source = source else {
            self.errorMessage = "No active story source."
            return
        }

        isLoading = true
        errorMessage = nil

        Task {
            do {
                self.isDownloaded = (try await downloadManager.isStoryDownloaded(storyId: parsedStoryId)).boolValue

                let detailResult = try await source.getStoryDetail(rawId: parsedStoryId.rawId)
                if let detail = detailResult.getOrNull() as? StoryDetail {
                    self.story = detail
                    try? await localCache.cacheStory(story: detail.story, accessedAt: Int64(Date().timeIntervalSince1970 * 1000))
                } else if let err = detailResult.errorOrNull() {
                    self.errorMessage = err.message
                }

                // Check cached chapters
                let cachedChapters = (try? await localCache.getCachedChapters(storyId: parsedStoryId)) ?? []
                if !cachedChapters.isEmpty {
                    self.chapters = cachedChapters
                } else {
                    let chaptersResult = try await source.getChapterList(rawId: parsedStoryId.rawId)
                    if let chapterList = chaptersResult.getOrNull() as? [Chapter] {
                        self.chapters = chapterList
                        try? await localCache.cacheChapters(chapters: chapterList)
                    }
                }

                self.isLoading = false
            } catch {
                self.errorMessage = error.localizedDescription
                self.isLoading = false
            }
        }
    }

    public func startDownload() {
        guard let storyDetail = story, let downloadUrl = chapters.first?.url else { return }

        isDownloading = true
        downloadProgress = 0.05
        downloadErrorMessage = nil
        downloadSuccessMessage = nil

        Task {
            do {
                _ = try await downloadManager.startDownload(story: storyDetail.story, downloadUrl: downloadUrl) { progress in
                    Task { @MainActor in
                        self.downloadProgress = progress.progress
                        if progress.status == .completed {
                            self.isDownloading = false
                            self.isDownloaded = true
                            self.isInLibrary = true
                            self.downloadSuccessMessage = "Đã tải xong toàn bộ tác phẩm!"
                            let parsedStoryId = StoryId.companion.from(compositeValue: storyDetail.story.id.value)
                            if let reloaded = try? await self.localCache.getCachedChapters(storyId: parsedStoryId) {
                                self.chapters = reloaded
                            }
                        } else if progress.status == .failed {
                            self.isDownloading = false
                            self.downloadErrorMessage = progress.errorMessage ?? "Tải truyện thất bại. Vui lòng thử lại."
                        }
                    }
                }
            } catch {
                Task { @MainActor in
                    self.isDownloading = false
                    self.downloadErrorMessage = error.localizedDescription
                }
            }
        }
    }

    public func toggleLibrary() {
        self.isInLibrary.toggle()
    }
}
