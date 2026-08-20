package com.slowbuild.storyverse.data.network

import com.slowbuild.storyverse.data.network.dto.AuthorDto
import com.slowbuild.storyverse.data.network.dto.CategoryDto
import com.slowbuild.storyverse.data.network.dto.ChapterContentDto
import com.slowbuild.storyverse.data.network.dto.ChapterDto
import com.slowbuild.storyverse.data.network.dto.StoryDetailDto
import com.slowbuild.storyverse.data.network.dto.StoryDto
import com.slowbuild.storyverse.data.network.dto.toDomain
import com.slowbuild.storyverse.domain.model.StoryId
import com.slowbuild.storyverse.domain.model.StoryOrigin
import com.slowbuild.storyverse.domain.model.StorySourceInfo
import com.slowbuild.storyverse.domain.model.StoryStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NetworkDtoMapperTest {

    @Test
    fun story_dto_maps_to_domain_correctly() {
        val storyDto = StoryDto(
            id = "truyen-123",
            title = "Đấu Phá Thương Khung",
            coverUrl = "https://example.com/cover.jpg",
            authors = listOf(AuthorDto("1", "Thiên Tằm Thổ Đậu")),
            categories = listOf(CategoryDto("1", "Huyền Huyễn", "huyen-huyen")),
            status = "completed",
            description = "Nơi đây thuộc về đấu khí thế giới...",
            totalChapters = 1648,
            latestChapterTitle = "Đại kết cục",
            updatedAt = 1700000000L
        )

        val domainStory = storyDto.toDomain("truyenfull", StoryOrigin.REMOTE)

        assertEquals("truyenfull::truyen-123", domainStory.id.value)
        assertEquals("Đấu Phá Thương Khung", domainStory.title)
        assertEquals(StoryStatus.COMPLETED, domainStory.status)
        assertEquals(StoryOrigin.REMOTE, domainStory.origin)
        assertEquals("Thiên Tằm Thổ Đậu", domainStory.authorNames)
        assertEquals(1, domainStory.categories.size)
    }

    @Test
    fun story_detail_dto_maps_to_domain_correctly() {
        val detailDto = StoryDetailDto(
            story = StoryDto(id = "story-1", title = "Vũ Động Càn Khôn"),
            fullDescription = "Mô tả đầy đủ...",
            rating = 4.9f,
            views = 500000L,
            chapters = listOf(
                ChapterDto(id = "ch-1", index = 1, title = "Chương 1")
            )
        )
        val sourceInfo = StorySourceInfo(id = "source-test", name = "Test Source")

        val domainDetail = detailDto.toDomain(sourceInfo)

        assertEquals("Vũ Động Càn Khôn", domainDetail.story.title)
        assertEquals(4.9f, domainDetail.rating)
        assertEquals(1, domainDetail.chapters.size)
        assertEquals("source-test::story-1", domainDetail.chapters[0].storyId.value)
    }

    @Test
    fun chapter_content_dto_maps_and_normalizes_paragraphs() {
        val rawText = "Đoạn văn thứ nhất.\n\nĐoạn văn thứ hai.\n\nĐoạn văn thứ ba."
        val contentDto = ChapterContentDto(
            chapterId = "ch-1",
            title = "Chương 1",
            content = rawText
        )
        val storyId = StoryId.create("source-1", "story-1")

        val domainContent = contentDto.toDomain(storyId)

        assertEquals("ch-1", domainContent.chapterId)
        assertEquals(storyId, domainContent.storyId)
        assertEquals(3, domainContent.paragraphs.size)
        assertTrue(domainContent.wordCount > 0)
    }
}
