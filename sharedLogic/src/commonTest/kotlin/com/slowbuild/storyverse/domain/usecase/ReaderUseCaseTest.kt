package com.slowbuild.storyverse.domain.usecase

import com.slowbuild.storyverse.data.local.LocalStoryCache
import com.slowbuild.storyverse.data.local.room.StoryVerseDatabase
import com.slowbuild.storyverse.data.local.room.createInMemoryDatabase
import com.slowbuild.storyverse.data.repository.RoomReaderRepository
import com.slowbuild.storyverse.data.source.StorySourceRegistryImpl
import com.slowbuild.storyverse.data.source.stub.StubStorySource
import com.slowbuild.storyverse.domain.model.StoryId
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

import com.slowbuild.storyverse.data.reader.ReaderPreferencesRepositoryImpl
import com.slowbuild.storyverse.domain.reader.ReaderFontFamily
import com.slowbuild.storyverse.domain.reader.ReaderThemePreset

class ReaderUseCaseTest {

    private lateinit var database: StoryVerseDatabase
    private lateinit var readerUseCase: ReaderUseCase

    @BeforeTest
    fun setUp() {
        database = createInMemoryDatabase()
        val localCache = LocalStoryCache(
            storyDao = database.storyDao(),
            chapterDao = database.chapterDao(),
            chapterContentDao = database.chapterContentDao()
        )
        val readerRepo = RoomReaderRepository(
            readingProgressDao = database.readingProgressDao(),
            bookmarkDao = database.bookmarkDao(),
            historyDao = database.historyDao()
        )
        val registry = StorySourceRegistryImpl(listOf(StubStorySource()))
        val prefsRepo = ReaderPreferencesRepositoryImpl()

        readerUseCase = ReaderUseCase(
            storySourceRegistry = registry,
            localStoryCache = localCache,
            readerRepository = readerRepo,
            readerPreferencesRepository = prefsRepo
        )
    }

    @AfterTest
    fun tearDown() {
        database.close()
    }

    @Test
    fun readerUseCase_loads_chapter_session_and_records_progress() = runTest {
        val storyId = StoryId.create("stub_source", "pham-nhan-tu-tien")
        val chapterId = "pham-nhan-tu-tien_1"

        val result = readerUseCase.loadChapterSession(storyId, chapterId)
        assertTrue(result.isSuccess)

        val session = result.getOrNull()
        assertNotNull(session)
        assertEquals("Chương 1: Tu Tiên Bí Mật", session.content.title)
        assertTrue(session.wordCount > 0)
        assertTrue(session.chapters.size >= 2)
        assertEquals("pham-nhan-tu-tien_2", session.nextChapter?.id)

        val progress = readerUseCase.observeReadingProgress(storyId).first()
        assertNotNull(progress)
        assertEquals(chapterId, progress.lastReadChapterId)
    }

    @Test
    fun readerUseCase_toggles_bookmarks() = runTest {
        val storyId = StoryId.create("stub_source", "pham-nhan-tu-tien")
        val sessionResult = readerUseCase.loadChapterSession(storyId, "pham-nhan-tu-tien_1")
        val session = sessionResult.getOrNull()!!

        // Add bookmark
        val addResult = readerUseCase.toggleBookmark(session.story!!, session.currentChapter)
        assertTrue(addResult.isSuccess)
        assertTrue(addResult.getOrNull() == true)

        var bookmarks = readerUseCase.observeBookmarks(storyId).first()
        assertEquals(1, bookmarks.size)
        assertEquals("pham-nhan-tu-tien_1", bookmarks[0].chapterId)

        // Remove bookmark
        val removeResult = readerUseCase.toggleBookmark(session.story!!, session.currentChapter)
        assertTrue(removeResult.isSuccess)
        assertFalse(removeResult.getOrNull() == true)

        bookmarks = readerUseCase.observeBookmarks(storyId).first()
        assertEquals(0, bookmarks.size)
    }

    @Test
    fun readerUseCase_updates_and_observes_preferences() = runTest {
        assertEquals(17f, readerUseCase.preferences.value.fontSize)

        readerUseCase.setFontSize(22f)
        assertEquals(22f, readerUseCase.preferences.value.fontSize)

        readerUseCase.setThemePreset(ReaderThemePreset.SEPIA)
        assertEquals(ReaderThemePreset.SEPIA, readerUseCase.preferences.value.themePreset)

        readerUseCase.setFontFamily(ReaderFontFamily.SERIF)
        assertEquals(ReaderFontFamily.SERIF, readerUseCase.preferences.value.fontFamily)

        readerUseCase.setLineSpacing(1.8f)
        assertEquals(1.8f, readerUseCase.preferences.value.lineSpacingMultiplier)
    }
}
