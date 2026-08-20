package com.slowbuild.storyverse.domain.source

import com.slowbuild.storyverse.data.source.stub.StubStorySource
import com.slowbuild.storyverse.domain.model.StoryStatus
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class StubStorySourceTest {

    private val source = StubStorySource()

    @Test
    fun getHomeSections_returns_sections_with_stories() = runTest {
        val result = source.getHomeSections()

        assertTrue(result.isSuccess)
        val sections = result.getOrNull()
        assertNotNull(sections)
        assertTrue(sections.isNotEmpty())
        assertEquals("featured", sections[0].id)
        assertTrue(sections[0].stories.isNotEmpty())
    }

    @Test
    fun getLatestUpdates_returns_paginated_stories() = runTest {
        val result = source.getLatestUpdates(page = 1)

        assertTrue(result.isSuccess)
        val page = result.getOrNull()
        assertNotNull(page)
        assertEquals(1, page.page)
        assertTrue(page.stories.isNotEmpty())
    }

    @Test
    fun search_filters_by_query_and_category() = runTest {
        val queryResult = source.search("phàm nhân", page = 1)
        assertTrue(queryResult.isSuccess)
        val queryPage = queryResult.getOrNull()
        assertNotNull(queryPage)
        assertEquals(1, queryPage.stories.size)
        assertEquals("Phàm Nhân Tu Tiên", queryPage.stories[0].title)

        val categoryFilterResult = source.search(
            query = "",
            page = 1,
            filter = StoryFilter(category = "huyen-huyen")
        )
        assertTrue(categoryFilterResult.isSuccess)
        val categoryPage = categoryFilterResult.getOrNull()
        assertNotNull(categoryPage)
        assertTrue(categoryPage.stories.size >= 2)
    }

    @Test
    fun getStoryDetail_and_chapters_returns_rich_data() = runTest {
        val detailResult = source.getStoryDetail("pham-nhan-tu-tien")

        assertTrue(detailResult.isSuccess)
        val detail = detailResult.getOrNull()
        assertNotNull(detail)
        assertEquals("Phàm Nhân Tu Tiên", detail.story.title)
        assertEquals(20, detail.chapters.size)
        assertEquals(StubStorySource.ID, detail.sourceInfo.id)

        val chapterListResult = source.getChapterList("pham-nhan-tu-tien")
        assertTrue(chapterListResult.isSuccess)
        val chapters = chapterListResult.getOrNull()
        assertNotNull(chapters)
        assertEquals(20, chapters.size)
        assertEquals(1, chapters[0].index)
    }

    @Test
    fun getChapterContent_returns_paragraphs_and_word_count() = runTest {
        val contentResult = source.getChapterContent("pham-nhan-tu-tien_1")

        assertTrue(contentResult.isSuccess)
        val content = contentResult.getOrNull()
        assertNotNull(content)
        assertEquals("pham-nhan-tu-tien_1", content.chapterId)
        assertTrue(content.paragraphs.isNotEmpty())
        assertTrue(content.wordCount > 0)
    }
}
