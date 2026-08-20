package com.slowbuild.storyverse.domain.source

import com.slowbuild.storyverse.data.source.GeneratedStorySource
import com.slowbuild.storyverse.data.source.LocalStorySource
import com.slowbuild.storyverse.data.source.StorySourceRegistryImpl
import com.slowbuild.storyverse.data.source.stub.StubStorySource
import com.slowbuild.storyverse.domain.model.Chapter
import com.slowbuild.storyverse.domain.model.ChapterContent
import com.slowbuild.storyverse.domain.model.StoryDetail
import com.slowbuild.storyverse.domain.model.StoryOrigin
import com.slowbuild.storyverse.core.result.AppResult
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class StorySourceRegistryTest {

    @Test
    fun registry_registers_and_retrieves_sources() {
        val registry = StorySourceRegistryImpl()
        val stubSource = StubStorySource()

        assertEquals(0, registry.getAllSources().size)

        registry.register(stubSource)

        assertEquals(1, registry.getAllSources().size)
        assertEquals(stubSource, registry.getSource(StubStorySource.ID))
        assertEquals(stubSource, registry.requireSource(StubStorySource.ID))
    }

    @Test
    fun registry_unregisters_source_correctly() {
        val stubSource = StubStorySource()
        val registry = StorySourceRegistryImpl(listOf(stubSource))

        assertEquals(1, registry.getAllSources().size)
        registry.unregister(StubStorySource.ID)

        assertEquals(0, registry.getAllSources().size)
        assertNull(registry.getSource(StubStorySource.ID))
        assertFailsWith<NoSuchElementException> {
            registry.requireSource(StubStorySource.ID)
        }
    }

    @Test
    fun registry_filters_sources_by_origin() {
        val registry = StorySourceRegistryImpl()
        val remoteSource = StubStorySource()
        
        val localSource = object : LocalStorySource() {
            override suspend fun getHomeSections(): AppResult<List<StorySection>> = AppResult.Success(emptyList())
            override suspend fun getLatestUpdates(page: Int): AppResult<StoryPage> = AppResult.Success(StoryPage())
            override suspend fun getPopular(page: Int): AppResult<StoryPage> = AppResult.Success(StoryPage())
            override suspend fun search(query: String, page: Int, filter: StoryFilter?): AppResult<StoryPage> = AppResult.Success(StoryPage())
            override suspend fun getStoryDetail(rawId: String): AppResult<StoryDetail> = throw NotImplementedError()
            override suspend fun getChapterList(rawId: String): AppResult<List<Chapter>> = AppResult.Success(emptyList())
            override suspend fun getChapterContent(chapterId: String): AppResult<ChapterContent> = throw NotImplementedError()
        }

        registry.register(remoteSource)
        registry.register(localSource)

        val remoteSources = registry.getSourcesByOrigin(StoryOrigin.REMOTE)
        val localSources = registry.getSourcesByOrigin(StoryOrigin.LOCAL_EPUB)

        assertEquals(1, remoteSources.size)
        assertEquals(1, localSources.size)
        assertEquals(StubStorySource.ID, remoteSources[0].metadata.id)
        assertEquals(LocalStorySource.ID, localSources[0].metadata.id)
    }

    @Test
    fun registry_observe_sources_state_flow_emits_updates() {
        val registry = StorySourceRegistryImpl()
        val stubSource = StubStorySource()

        val initialState = registry.observeSources().value
        assertEquals(0, initialState.size)

        registry.register(stubSource)
        val updatedState = registry.observeSources().value
        assertEquals(1, updatedState.size)
        assertEquals(StubStorySource.ID, updatedState[0].id)
    }
}
