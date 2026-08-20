package com.slowbuild.storyverse.domain.model

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DomainModelsSerializationTest {

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    @Test
    fun story_and_story_detail_serialization_roundtrip() {
        val story = Story(
            id = StoryId.create("source_1", "story_1"),
            title = "Phàm Nhân Tu Tiên",
            coverUrl = "https://example.com/cover.jpg",
            authors = listOf(Author(id = "1", name = "Vong Ngữ")),
            categories = listOf(Category(id = "1", name = "Tiên Hiệp", slug = "tien-hiep")),
            status = StoryStatus.COMPLETED,
            origin = StoryOrigin.REMOTE,
            description = "Hành trình tu tiên của Hàn Lập",
            totalChapters = 2446,
            latestChapterTitle = "Hồi kết",
            updatedAt = 1700000000000L,
            inLibrary = true
        )

        val storyJson = json.encodeToString(Story.serializer(), story)
        val decodedStory = json.decodeFromString(Story.serializer(), storyJson)
        assertEquals(story, decodedStory)
        assertEquals("Vong Ngữ", decodedStory.authorNames)

        val storyDetail = StoryDetail(
            story = story,
            sourceInfo = StorySourceInfo(
                id = "source_1",
                name = "TruyenFull",
                baseUrl = "https://example.com"
            ),
            fullDescription = "Chi tiết truyện tu tiên...",
            rating = 4.8f,
            views = 1200000L,
            isBookmarked = true,
            chapters = listOf(
                Chapter(
                    id = "ch_1",
                    storyId = story.id,
                    index = 1,
                    title = "Chương 1: Sơn thôn thiếu niên",
                    url = "https://example.com/ch1",
                    isDownloaded = true,
                    isRead = true
                )
            ),
            extraMetadata = mapOf("uploader" to "Admin")
        )

        val detailJson = json.encodeToString(StoryDetail.serializer(), storyDetail)
        val decodedDetail = json.decodeFromString(StoryDetail.serializer(), detailJson)
        assertEquals(storyDetail, decodedDetail)
    }

    @Test
    fun chapter_content_serialization_roundtrip() {
        val content = ChapterContent(
            chapterId = "ch_1",
            storyId = StoryId.create("source_1", "story_1"),
            title = "Chương 1: Sơn thôn thiếu niên",
            content = "Hàn Lập sinh ra trong một thôn nghèo...",
            paragraphs = listOf(
                "Hàn Lập sinh ra trong một thôn nghèo.",
                "Năm mười tuổi, phụ thân quyết định gửi hắn đi."
            ),
            wordCount = 1200,
            sourceInfo = "TruyenFull"
        )

        val contentJson = json.encodeToString(ChapterContent.serializer(), content)
        val decodedContent = json.decodeFromString(ChapterContent.serializer(), contentJson)
        assertEquals(content, decodedContent)
        assertEquals(2, decodedContent.paragraphs.size)
    }

    @Test
    fun reading_progress_and_bookmark_serialization_roundtrip() {
        val progress = ReadingProgress(
            storyId = StoryId.create("source_1", "story_1"),
            lastReadChapterId = "ch_10",
            lastReadChapterIndex = 10,
            scrollOffset = 350,
            progressPercentage = 0.45f,
            lastReadAt = 1700000000000L
        )

        val progressJson = json.encodeToString(ReadingProgress.serializer(), progress)
        val decodedProgress = json.decodeFromString(ReadingProgress.serializer(), progressJson)
        assertEquals(progress, decodedProgress)

        val bookmark = Bookmark(
            id = "bm_1",
            storyId = StoryId.create("source_1", "story_1"),
            chapterId = "ch_10",
            chapterTitle = "Chương 10: Nhập môn",
            paragraphIndex = 5,
            note = "Đoạn bắt đầu vào môn phái",
            createdAt = 1700000000000L
        )

        val bookmarkJson = json.encodeToString(Bookmark.serializer(), bookmark)
        val decodedBookmark = json.decodeFromString(Bookmark.serializer(), bookmarkJson)
        assertEquals(bookmark, decodedBookmark)
    }

    @Test
    fun download_state_serialization_roundtrip() {
        val download = ChapterDownload(
            storyId = StoryId.create("source_1", "story_1"),
            chapterId = "ch_1",
            chapterTitle = "Chương 1",
            chapterIndex = 1,
            status = DownloadStatus.COMPLETED,
            progress = 1.0f,
            downloadedAt = 1700000000000L
        )

        val downloadJson = json.encodeToString(ChapterDownload.serializer(), download)
        val decodedDownload = json.decodeFromString(ChapterDownload.serializer(), downloadJson)
        assertEquals(download, decodedDownload)

        val summary = StoryDownloadSummary(
            storyId = StoryId.create("source_1", "story_1"),
            totalChapters = 100,
            downloadedChapters = 50,
            status = DownloadStatus.DOWNLOADING
        )

        assertEquals(0.5f, summary.progress)
    }
}
