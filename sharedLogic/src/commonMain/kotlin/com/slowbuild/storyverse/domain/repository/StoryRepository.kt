package com.slowbuild.storyverse.domain.repository

import com.slowbuild.storyverse.core.result.AppResult
import com.slowbuild.storyverse.domain.model.Chapter
import com.slowbuild.storyverse.domain.model.ChapterContent
import com.slowbuild.storyverse.domain.model.Story
import com.slowbuild.storyverse.domain.model.StoryDetail
import com.slowbuild.storyverse.domain.model.StoryId
import kotlinx.coroutines.flow.Flow

interface StoryRepository {
    fun getDiscoverStories(category: String? = null): Flow<AppResult<List<Story>>>
    
    fun searchStories(query: String, page: Int = 1, category: String? = null): Flow<AppResult<List<Story>>>
    
    fun getStoryDetail(storyId: StoryId, forceRefresh: Boolean = false): Flow<AppResult<StoryDetail>>
    
    fun getChapterList(storyId: StoryId, forceRefresh: Boolean = false): Flow<AppResult<List<Chapter>>>
    
    fun getChapterContent(storyId: StoryId, chapterId: String): Flow<AppResult<ChapterContent>>
    
    fun observeLibraryStories(): Flow<List<Story>>
    
    suspend fun toggleLibrary(storyId: StoryId, inLibrary: Boolean): AppResult<Unit>
}
