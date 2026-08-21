package com.slowbuild.storyverse.domain.usecase

import com.slowbuild.storyverse.core.logging.AppLogger
import com.slowbuild.storyverse.core.result.AppError
import com.slowbuild.storyverse.core.result.AppResult
import com.slowbuild.storyverse.core.time.currentTimeMillis
import com.slowbuild.storyverse.data.local.LocalStoryCache
import com.slowbuild.storyverse.domain.model.Bookmark
import com.slowbuild.storyverse.domain.model.Chapter
import com.slowbuild.storyverse.domain.model.ChapterContent
import com.slowbuild.storyverse.domain.model.HistoryEntry
import com.slowbuild.storyverse.domain.model.ReadingProgress
import com.slowbuild.storyverse.domain.model.Story
import com.slowbuild.storyverse.domain.model.StoryId
import com.slowbuild.storyverse.domain.repository.ReaderRepository
import com.slowbuild.storyverse.domain.source.StorySourceRegistry
import com.slowbuild.storyverse.domain.reader.ReaderFontFamily
import com.slowbuild.storyverse.domain.reader.ReaderPreferences
import com.slowbuild.storyverse.domain.reader.ReaderPreferencesRepository
import com.slowbuild.storyverse.domain.reader.ReaderThemePreset
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

data class ReaderSessionData(
    val story: Story?,
    val currentChapter: Chapter,
    val content: ChapterContent,
    val chapters: List<Chapter>,
    val prevChapter: Chapter?,
    val nextChapter: Chapter?,
    val wordCount: Int,
    val estimatedReadMinutes: Int
)

class ReaderUseCase(
    private val storySourceRegistry: StorySourceRegistry,
    private val localStoryCache: LocalStoryCache,
    private val readerRepository: ReaderRepository,
    private val readerPreferencesRepository: ReaderPreferencesRepository
) {
    val preferences: StateFlow<ReaderPreferences> = readerPreferencesRepository.preferences

    fun updatePreferences(transform: (ReaderPreferences) -> ReaderPreferences) =
        readerPreferencesRepository.updatePreferences(transform)

    fun setFontSize(size: Float) = readerPreferencesRepository.setFontSize(size)
    fun setFontFamily(fontFamily: ReaderFontFamily) = readerPreferencesRepository.setFontFamily(fontFamily)
    fun setLineSpacing(multiplier: Float) = readerPreferencesRepository.setLineSpacing(multiplier)
    fun setThemePreset(preset: ReaderThemePreset) = readerPreferencesRepository.setThemePreset(preset)
    fun setHorizontalPadding(paddingDp: Float) = readerPreferencesRepository.setHorizontalPadding(paddingDp)

    fun observeHistory(limit: Int = 50): Flow<List<HistoryEntry>> = readerRepository.observeHistory(limit)
    suspend fun clearHistory(): AppResult<Unit> = readerRepository.clearHistory()
    suspend fun deleteBookmark(bookmarkId: String): AppResult<Unit> = readerRepository.removeBookmark(bookmarkId)
    suspend fun loadChapterSession(
        storyId: StoryId,
        chapterId: String,
        prefetchScope: CoroutineScope? = null
    ): AppResult<ReaderSessionData> {
        // 1. Fetch story and chapter list from cache or source
        var story = localStoryCache.getCachedStory(storyId)
        var chapters = localStoryCache.getCachedChapters(storyId)

        val source = storySourceRegistry.getSource(storyId.sourceId)
            ?: storySourceRegistry.getDefaultSource()

        if (chapters.isEmpty() && source != null) {
            when (val chaptersResult = source.getChapterList(storyId.rawId)) {
                is AppResult.Success -> {
                    chapters = chaptersResult.data
                    localStoryCache.cacheChapters(chapters)
                }
                is AppResult.Error -> AppLogger.w("ReaderUseCase") { "Failed to load chapter list: ${chaptersResult.error.message}" }
            }
        }

        if (story == null && source != null) {
            when (val detailResult = source.getStoryDetail(storyId.rawId)) {
                is AppResult.Success -> {
                    story = detailResult.data.story
                    localStoryCache.cacheStory(story)
                }
                is AppResult.Error -> AppLogger.w("ReaderUseCase") { "Failed to load story detail: ${detailResult.error.message}" }
            }
        }

        val currentChapter = chapters.find { it.id == chapterId }
            ?: Chapter(id = chapterId, storyId = storyId, title = "Chương hiện tại", index = 1)

        val currentIndex = chapters.indexOfFirst { it.id == chapterId }
        val prevChapter = if (currentIndex > 0) chapters[currentIndex - 1] else null
        val nextChapter = if (currentIndex >= 0 && currentIndex < chapters.size - 1) chapters[currentIndex + 1] else null

        // 2. Load Chapter Content (Check local cache first)
        var content = localStoryCache.getCachedChapterContent(chapterId)
        if (content == null) {
            if (source == null) {
                return AppResult.Error(AppError.Source("Không tìm thấy nguồn truyện"))
            }
            when (val contentResult = source.getChapterContent(chapterId)) {
                is AppResult.Success -> {
                    content = contentResult.data
                    localStoryCache.cacheChapterContent(content)
                }
                is AppResult.Error -> return contentResult
            }
        }

        // 3. Compute word count and estimated reading time
        val totalWords = content.paragraphs.sumOf { p -> p.split("\\s+".toRegex()).count { it.isNotBlank() } }
        val estimatedMinutes = (totalWords / 200).coerceAtLeast(1)

        // 4. Save Reading Progress and History Record
        val now = currentTimeMillis()
        if (story != null) {
            val progress = ReadingProgress(
                storyId = storyId,
                lastReadChapterId = chapterId,
                lastReadChapterIndex = currentChapter.index,
                scrollOffset = 0,
                progressPercentage = if (chapters.isNotEmpty()) ((currentChapter.index.toFloat() / chapters.size) * 100f).coerceIn(0f, 100f) else 0f,
                lastReadAt = now
            )
            readerRepository.saveReadingProgress(progress)

            val historyEntry = HistoryEntry(
                story = story,
                lastReadChapterId = chapterId,
                lastReadChapterTitle = currentChapter.title,
                lastReadChapterIndex = currentChapter.index,
                lastReadAt = now
            )
            readerRepository.recordHistory(historyEntry)
        }

        // 5. Asynchronous Background Prefetch of Adjacent Chapters
        prefetchScope?.launch(Dispatchers.IO) {
            if (nextChapter != null && localStoryCache.getCachedChapterContent(nextChapter.id) == null) {
                source?.getChapterContent(nextChapter.id)?.let { res ->
                    if (res is AppResult.Success) localStoryCache.cacheChapterContent(res.data)
                }
            }
            if (prevChapter != null && localStoryCache.getCachedChapterContent(prevChapter.id) == null) {
                source?.getChapterContent(prevChapter.id)?.let { res ->
                    if (res is AppResult.Success) localStoryCache.cacheChapterContent(res.data)
                }
            }
        }

        return AppResult.Success(
            ReaderSessionData(
                story = story,
                currentChapter = currentChapter,
                content = content,
                chapters = chapters,
                prevChapter = prevChapter,
                nextChapter = nextChapter,
                wordCount = totalWords,
                estimatedReadMinutes = estimatedMinutes
            )
        )
    }

    suspend fun saveScrollPosition(
        storyId: StoryId,
        chapterId: String,
        chapterIndex: Int,
        scrollOffset: Int,
        progressPercentage: Float
    ): AppResult<Unit> {
        val progress = ReadingProgress(
            storyId = storyId,
            lastReadChapterId = chapterId,
            lastReadChapterIndex = chapterIndex,
            scrollOffset = scrollOffset,
            progressPercentage = progressPercentage,
            lastReadAt = currentTimeMillis()
        )
        return readerRepository.saveReadingProgress(progress)
    }

    suspend fun toggleBookmark(
        story: Story,
        chapter: Chapter,
        snippet: String? = null
    ): AppResult<Boolean> {
        val bookmarks = readerRepository.observeBookmarks(story.id).firstOrNull().orEmpty()
        val existing = bookmarks.find { it.chapterId == chapter.id }

        return if (existing != null) {
            readerRepository.removeBookmark(existing.id)
            AppResult.Success(false)
        } else {
            val newBookmark = Bookmark(
                id = "${story.id.value}_${chapter.id}_${currentTimeMillis()}",
                storyId = story.id,
                chapterId = chapter.id,
                chapterTitle = chapter.title,
                paragraphIndex = 0,
                note = snippet,
                createdAt = currentTimeMillis()
            )
            readerRepository.addBookmark(newBookmark)
            AppResult.Success(true)
        }
    }

    fun observeBookmarks(storyId: StoryId): Flow<List<Bookmark>> {
        return readerRepository.observeBookmarks(storyId)
    }

    fun observeReadingProgress(storyId: StoryId): Flow<ReadingProgress?> {
        return readerRepository.observeReadingProgress(storyId)
    }
}
