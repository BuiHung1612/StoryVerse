package com.slowbuild.storyverse.data.local.room

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.slowbuild.storyverse.data.local.LocalStoryCache
import com.slowbuild.storyverse.data.local.room.entity.ChapterContentEntity
import com.slowbuild.storyverse.data.local.room.entity.ChapterEntity
import com.slowbuild.storyverse.data.local.room.entity.DownloadEntity
import com.slowbuild.storyverse.data.local.room.entity.StoryEntity
import com.slowbuild.storyverse.data.repository.RoomReaderRepository
import com.slowbuild.storyverse.domain.model.Bookmark
import com.slowbuild.storyverse.domain.model.Chapter
import com.slowbuild.storyverse.domain.model.ChapterContent
import com.slowbuild.storyverse.domain.model.DownloadStatus
import com.slowbuild.storyverse.domain.model.HistoryEntry
import com.slowbuild.storyverse.domain.model.ReadingProgress
import com.slowbuild.storyverse.domain.model.Story
import com.slowbuild.storyverse.domain.model.StoryDownloadSummary
import com.slowbuild.storyverse.domain.model.StoryId
import com.slowbuild.storyverse.domain.model.StoryOrigin
import com.slowbuild.storyverse.domain.model.StoryStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RoomDatabaseTest {

    private lateinit var database: StoryVerseDatabase
    private lateinit var readerRepository: RoomReaderRepository
    private lateinit var localCache: LocalStoryCache

    @BeforeTest
    fun setUp() {
        database = createInMemoryDatabase()

        readerRepository = RoomReaderRepository(
            readingProgressDao = database.readingProgressDao(),
            bookmarkDao = database.bookmarkDao(),
            historyDao = database.historyDao()
        )

        localCache = LocalStoryCache(
            storyDao = database.storyDao(),
            chapterDao = database.chapterDao(),
            chapterContentDao = database.chapterContentDao()
        )
    }

    @AfterTest
    fun tearDown() {
        database.close()
    }

    @Test
    fun story_dao_inserts_and_queries_story_correctly() = runTest {
        val story = Story(
            id = StoryId.create("test_src", "story_1"),
            title = "Tiên Nghịch",
            coverUrl = "https://example.com/cover.jpg",
            authors = emptyList(),
            categories = emptyList(),
            status = StoryStatus.COMPLETED,
            origin = StoryOrigin.REMOTE,
            description = "Mô tả truyện Tiên Nghịch",
            totalChapters = 2000,
            latestChapterTitle = "Chương 2000: Đại Kết Cục",
            updatedAt = null
        )

        localCache.cacheStory(story, accessedAt = 1000L)
        val retrieved = localCache.getCachedStory(story.id)

        assertNotNull(retrieved)
        assertEquals(story.id, retrieved.id)
        assertEquals("Tiên Nghịch", retrieved.title)
        assertEquals(2000, retrieved.totalChapters)
    }

    @Test
    fun chapter_dao_and_content_dao_cache_and_retrieve_content() = runTest {
        val storyId = StoryId.create("test_src", "story_2")
        val chapters = listOf(
            Chapter(
                id = "story_2_ch_1",
                storyId = storyId,
                index = 1,
                title = "Chương 1: Khởi Đầu",
                url = null,
                isDownloaded = false,
                isRead = false,
                wordCount = 3000,
                publishedAt = null
            ),
            Chapter(
                id = "story_2_ch_2",
                storyId = storyId,
                index = 2,
                title = "Chương 2: Bước Chân Đầu Tiên",
                url = null,
                isDownloaded = false,
                isRead = false,
                wordCount = 3500,
                publishedAt = null
            )
        )

        localCache.cacheChapters(chapters)
        val retrievedChapters = localCache.getCachedChapters(storyId)
        assertEquals(2, retrievedChapters.size)
        assertEquals("Chương 1: Khởi Đầu", retrievedChapters[0].title)

        val content = ChapterContent(
            chapterId = "story_2_ch_1",
            storyId = storyId,
            title = "Chương 1: Khởi Đầu",
            content = "Nội dung đoạn 1.\n\nNội dung đoạn 2.",
            paragraphs = listOf("Nội dung đoạn 1.", "Nội dung đoạn 2."),
            wordCount = 50,
            sourceInfo = "Test Source"
        )

        localCache.cacheChapterContent(content, cachedAt = 2000L)
        val retrievedContent = localCache.getCachedChapterContent("story_2_ch_1")
        assertNotNull(retrievedContent)
        assertEquals("Chương 1: Khởi Đầu", retrievedContent.title)
        assertEquals(2, retrievedContent.paragraphs.size)
    }

    @Test
    fun reading_progress_saves_and_observes_reactively() = runTest {
        val storyId = StoryId.create("test_src", "story_3")
        val progress = ReadingProgress(
            storyId = storyId,
            lastReadChapterId = "story_3_ch_5",
            lastReadChapterIndex = 5,
            scrollOffset = 150,
            progressPercentage = 0.45f,
            lastReadAt = 5000L
        )

        val saveResult = readerRepository.saveReadingProgress(progress)
        assertTrue(saveResult.isSuccess)

        val observedProgress = readerRepository.observeReadingProgress(storyId).first()
        assertNotNull(observedProgress)
        assertEquals(5, observedProgress.lastReadChapterIndex)
        assertEquals("story_3_ch_5", observedProgress.lastReadChapterId)
        assertEquals(150, observedProgress.scrollOffset)
        assertEquals(0.45f, observedProgress.progressPercentage)
    }

    @Test
    fun bookmarks_add_observe_and_remove_correctly() = runTest {
        val storyId = StoryId.create("test_src", "story_4")
        val bookmark = Bookmark(
            id = "bm_1",
            storyId = storyId,
            chapterId = "story_4_ch_1",
            chapterTitle = "Chương 1",
            paragraphIndex = 3,
            note = "Ghi chú cá nhân",
            createdAt = 6000L
        )

        val addResult = readerRepository.addBookmark(bookmark)
        assertTrue(addResult.isSuccess)

        val bookmarks = readerRepository.observeBookmarks(storyId).first()
        assertEquals(1, bookmarks.size)
        assertEquals("bm_1", bookmarks[0].id)
        assertEquals("Ghi chú cá nhân", bookmarks[0].note)

        val removeResult = readerRepository.removeBookmark("bm_1")
        assertTrue(removeResult.isSuccess)

        val bookmarksAfter = readerRepository.observeBookmarks(storyId).first()
        assertEquals(0, bookmarksAfter.size)
    }

    @Test
    fun reading_history_records_observes_and_clears() = runTest {
        val story = Story(
            id = StoryId.create("test_src", "story_5"),
            title = "Vũ Động Càn Khôn",
            coverUrl = null
        )
        val historyEntry1 = HistoryEntry(
            story = story,
            lastReadChapterId = "ch_10",
            lastReadChapterTitle = "Chương 10",
            lastReadChapterIndex = 10,
            lastReadAt = 10000L
        )

        val recordResult = readerRepository.recordHistory(historyEntry1)
        assertTrue(recordResult.isSuccess)

        val historyList = readerRepository.observeHistory(limit = 10).first()
        assertEquals(1, historyList.size)
        assertEquals("Vũ Động Càn Khôn", historyList[0].story.title)
        assertEquals("ch_10", historyList[0].lastReadChapterId)

        val clearResult = readerRepository.clearHistory()
        assertTrue(clearResult.isSuccess)

        val historyListAfter = readerRepository.observeHistory(limit = 10).first()
        assertEquals(0, historyListAfter.size)
    }

    @Test
    fun download_dao_persists_and_observes_downloads() = runTest {
        val downloadDao = database.downloadDao()
        val downloadEntity = DownloadEntity(
            storyId = "test_src::story_6",
            status = DownloadStatus.DOWNLOADING.name,
            downloadedChapters = 5,
            totalChapters = 50,
            bytesDownloaded = 500000L,
            totalBytes = 5000000L,
            errorMessage = null,
            updatedAt = 12000L
        )

        downloadDao.insertOrUpdate(downloadEntity)
        val retrieved = downloadDao.getDownloadState("test_src::story_6")

        assertNotNull(retrieved)
        val domainDownload = retrieved.toDomain()
        assertEquals(DownloadStatus.DOWNLOADING, domainDownload.status)
        assertEquals(5, domainDownload.downloadedChapters)
        assertEquals(50, domainDownload.totalChapters)
    }
}
