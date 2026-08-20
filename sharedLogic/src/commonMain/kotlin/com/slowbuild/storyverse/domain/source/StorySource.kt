package com.slowbuild.storyverse.domain.source

import com.slowbuild.storyverse.core.result.AppResult
import com.slowbuild.storyverse.domain.model.Chapter
import com.slowbuild.storyverse.domain.model.ChapterContent
import com.slowbuild.storyverse.domain.model.StoryDetail

interface StorySource {
    val metadata: StorySourceMetadata

    suspend fun getHomeSections(): AppResult<List<StorySection>>

    suspend fun getLatestUpdates(page: Int = 1): AppResult<StoryPage>

    suspend fun getPopular(page: Int = 1): AppResult<StoryPage>

    suspend fun search(query: String, page: Int = 1, filter: StoryFilter? = null): AppResult<StoryPage>

    suspend fun getStoryDetail(rawId: String): AppResult<StoryDetail>

    suspend fun getChapterList(rawId: String): AppResult<List<Chapter>>

    suspend fun getChapterContent(chapterId: String): AppResult<ChapterContent>
}
