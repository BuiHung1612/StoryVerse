package com.slowbuild.storyverse.data.repository

import com.slowbuild.storyverse.core.logging.AppLogger
import com.slowbuild.storyverse.core.result.AppError
import com.slowbuild.storyverse.core.result.AppResult
import com.slowbuild.storyverse.data.local.room.dao.BookmarkDao
import com.slowbuild.storyverse.data.local.room.dao.HistoryDao
import com.slowbuild.storyverse.data.local.room.dao.ReadingProgressDao
import com.slowbuild.storyverse.data.local.room.entity.BookmarkEntity
import com.slowbuild.storyverse.data.local.room.entity.HistoryEntity
import com.slowbuild.storyverse.data.local.room.entity.ReadingProgressEntity
import com.slowbuild.storyverse.domain.model.Bookmark
import com.slowbuild.storyverse.domain.model.HistoryEntry
import com.slowbuild.storyverse.domain.model.ReadingProgress
import com.slowbuild.storyverse.domain.model.StoryId
import com.slowbuild.storyverse.domain.repository.ReaderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomReaderRepository(
    private val readingProgressDao: ReadingProgressDao,
    private val bookmarkDao: BookmarkDao,
    private val historyDao: HistoryDao
) : ReaderRepository {

    override fun observeReadingProgress(storyId: StoryId): Flow<ReadingProgress?> {
        return readingProgressDao.observeProgressByStoryId(storyId.value)
            .map { it?.toDomain() }
    }

    override suspend fun saveReadingProgress(progress: ReadingProgress): AppResult<Unit> {
        return try {
            readingProgressDao.saveProgress(ReadingProgressEntity.fromDomain(progress))
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppLogger.e("RoomReaderRepo", e) { "Failed to save reading progress for ${progress.storyId}" }
            AppResult.Error(AppError.Database(e.message ?: "Failed to save reading progress"))
        }
    }

    override fun observeBookmarks(storyId: StoryId): Flow<List<Bookmark>> {
        return bookmarkDao.observeBookmarksByStory(storyId.value)
            .map { list -> list.map { it.toDomain() } }
    }

    override suspend fun addBookmark(bookmark: Bookmark): AppResult<Unit> {
        return try {
            bookmarkDao.insertBookmark(BookmarkEntity.fromDomain(bookmark))
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppLogger.e("RoomReaderRepo", e) { "Failed to add bookmark: ${bookmark.id}" }
            AppResult.Error(AppError.Database(e.message ?: "Failed to add bookmark"))
        }
    }

    override suspend fun removeBookmark(bookmarkId: String): AppResult<Unit> {
        return try {
            bookmarkDao.deleteBookmark(bookmarkId)
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppLogger.e("RoomReaderRepo", e) { "Failed to remove bookmark: $bookmarkId" }
            AppResult.Error(AppError.Database(e.message ?: "Failed to remove bookmark"))
        }
    }

    override fun observeHistory(limit: Int): Flow<List<HistoryEntry>> {
        return historyDao.observeHistory()
            .map { list -> list.take(limit).map { it.toDomain() } }
    }

    override suspend fun recordHistory(entry: HistoryEntry): AppResult<Unit> {
        return try {
            historyDao.insertOrUpdate(HistoryEntity.fromDomain(entry))
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppLogger.e("RoomReaderRepo", e) { "Failed to record history for ${entry.story.id}" }
            AppResult.Error(AppError.Database(e.message ?: "Failed to record history"))
        }
    }

    override suspend fun clearHistory(): AppResult<Unit> {
        return try {
            historyDao.clearAllHistory()
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppLogger.e("RoomReaderRepo", e) { "Failed to clear history" }
            AppResult.Error(AppError.Database(e.message ?: "Failed to clear history"))
        }
    }
}
