package com.slowbuild.storyverse.data.local

import com.slowbuild.storyverse.data.local.room.dao.ChapterContentDao
import com.slowbuild.storyverse.data.local.room.dao.ChapterDao
import com.slowbuild.storyverse.data.local.room.dao.StoryDao
import com.slowbuild.storyverse.data.local.room.entity.ChapterContentEntity
import com.slowbuild.storyverse.data.local.room.entity.ChapterEntity
import com.slowbuild.storyverse.data.local.room.entity.StoryEntity
import com.slowbuild.storyverse.domain.model.Chapter
import com.slowbuild.storyverse.domain.model.ChapterContent
import com.slowbuild.storyverse.domain.model.Story
import com.slowbuild.storyverse.domain.model.StoryId

class LocalStoryCache(
    private val storyDao: StoryDao,
    private val chapterDao: ChapterDao,
    private val chapterContentDao: ChapterContentDao
) {
    suspend fun cacheStory(story: Story, accessedAt: Long = 0L) {
        storyDao.insertOrUpdate(StoryEntity.fromDomain(story, accessedAt))
    }

    suspend fun getCachedStory(storyId: StoryId): Story? {
        return storyDao.getStoryById(storyId.value)?.toDomain()
    }

    suspend fun cacheChapters(chapters: List<Chapter>) {
        if (chapters.isNotEmpty()) {
            chapterDao.insertOrUpdateAll(chapters.map { ChapterEntity.fromDomain(it) })
        }
    }

    suspend fun getCachedChapters(storyId: StoryId): List<Chapter> {
        return chapterDao.getChaptersByStoryId(storyId.value).map { it.toDomain() }
    }

    suspend fun cacheChapterContent(content: ChapterContent, cachedAt: Long = 0L) {
        chapterContentDao.insertOrUpdate(ChapterContentEntity.fromDomain(content, cachedAt))
    }

    suspend fun cacheChapterContents(contents: List<ChapterContent>, cachedAt: Long = 0L) {
        if (contents.isNotEmpty()) {
            chapterContentDao.insertOrUpdateAll(contents.map { ChapterContentEntity.fromDomain(it, cachedAt) })
        }
    }

    suspend fun getCachedChapterContent(chapterId: String): ChapterContent? {
        return chapterContentDao.getContentByChapterId(chapterId)?.toDomain()
    }
}
