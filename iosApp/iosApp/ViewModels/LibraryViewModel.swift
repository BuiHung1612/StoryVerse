import Foundation
import SharedLogic

public enum LibraryTab: Int, CaseIterable, Identifiable {
    case saved = 0
    case history = 1
    case downloads = 2

    public var id: Int { rawValue }

    public var title: String {
        switch self {
        case .saved: return localizedString(AppStringKey.libraryTabFavorites)
        case .history: return localizedString(AppStringKey.libraryTabHistory)
        case .downloads: return localizedString(AppStringKey.libraryTabDownloads)
        }
    }
}

@MainActor
public final class LibraryViewModel: ObservableObject {
    @Published public var selectedTab: LibraryTab = .saved
    @Published public private(set) var savedStories: [Story] = []
    @Published public private(set) var historyEntries: [HistoryEntry] = []
    @Published public private(set) var isLoading: Bool = false

    private let readerRepository: ReaderRepository
    private let storySourceRegistry: StorySourceRegistry

    public init(
        readerRepository: ReaderRepository = KoinHelper().readerRepository,
        storySourceRegistry: StorySourceRegistry = KoinHelper().storySourceRegistry
    ) {
        self.readerRepository = readerRepository
        self.storySourceRegistry = storySourceRegistry
    }

    public func loadLibrary() {
        isLoading = true
        Task {
            // Load saved/cached sample stories if any
            if let source = storySourceRegistry.getDefaultSource() {
                do {
                    let popular = try await source.getPopular(page: 1)
                    if let page = popular.getOrNull() as? StoryPage {
                        self.savedStories = Array(page.stories.prefix(6))
                    }
                } catch {}
            }
            self.isLoading = false
        }
    }

    public func clearHistory() {
        Task {
            _ = try? await readerRepository.clearHistory()
            self.historyEntries = []
        }
    }
}
