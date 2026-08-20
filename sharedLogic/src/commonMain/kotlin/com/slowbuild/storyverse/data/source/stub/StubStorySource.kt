package com.slowbuild.storyverse.data.source.stub

import com.slowbuild.storyverse.core.result.AppError
import com.slowbuild.storyverse.core.result.AppResult
import com.slowbuild.storyverse.domain.model.Author
import com.slowbuild.storyverse.domain.model.Category
import com.slowbuild.storyverse.domain.model.Chapter
import com.slowbuild.storyverse.domain.model.ChapterContent
import com.slowbuild.storyverse.domain.model.Story
import com.slowbuild.storyverse.domain.model.StoryDetail
import com.slowbuild.storyverse.domain.model.StoryId
import com.slowbuild.storyverse.domain.model.StoryOrigin
import com.slowbuild.storyverse.domain.model.StoryStatus
import com.slowbuild.storyverse.domain.source.SectionType
import com.slowbuild.storyverse.domain.source.SourceCapabilities
import com.slowbuild.storyverse.domain.source.StoryFilter
import com.slowbuild.storyverse.domain.source.StoryPage
import com.slowbuild.storyverse.domain.source.StorySection
import com.slowbuild.storyverse.domain.source.StorySource
import com.slowbuild.storyverse.domain.source.StorySourceMetadata

class StubStorySource : StorySource {

    override val metadata: StorySourceMetadata = StorySourceMetadata(
        id = ID,
        name = "stub_source",
        displayName = "StoryVerse Demo Library",
        baseUrl = "https://demo.storyverse.com",
        origin = StoryOrigin.REMOTE,
        capabilities = SourceCapabilities(
            supportsSearch = true,
            supportsCategories = true,
            supportsRanking = true,
            supportsLatestUpdates = true,
            supportsPagination = true,
            supportsOfflineDownload = true
        )
    )

    private val stories = listOf(
        Story(
            id = StoryId.create(ID, "pham-nhan-tu-tien"),
            title = "Phàm Nhân Tu Tiên",
            coverUrl = "https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c",
            authors = listOf(Author("1", "Vong Ngữ")),
            categories = listOf(Category("1", "Tiên Hiệp", "tien-hiep"), Category("2", "Kiếm Hiệp", "kiem-hiep")),
            status = StoryStatus.COMPLETED,
            origin = StoryOrigin.REMOTE,
            description = "Hành trình tu tiên đầy gian nan của Hàn Lập từ một thiếu niên bình phàm.",
            totalChapters = 2446,
            latestChapterTitle = "Chương 2446: Phi thăng Tiên giới (Hết)",
            updatedAt = 1700000000000L
        ),
        Story(
            id = StoryId.create(ID, "dau-pha-thuong-khung"),
            title = "Đấu Phá Thương Khung",
            coverUrl = "https://images.unsplash.com/photo-1532012164546-f432f2e3777a",
            authors = listOf(Author("2", "Thiên Tằm Thổ Đậu")),
            categories = listOf(Category("3", "Huyền Huyễn", "huyen-huyen"), Category("4", "Dị Giới", "di-gioi")),
            status = StoryStatus.COMPLETED,
            origin = StoryOrigin.REMOTE,
            description = "Nơi đây là thế giới thuộc về Đấu Khí, Tiêu Viêm từng là thiên tài bỗng trở thành phế vật.",
            totalChapters = 1648,
            latestChapterTitle = "Chương 1648: Viêm Đế (Hết)",
            updatedAt = 1690000000000L
        ),
        Story(
            id = StoryId.create(ID, "vu-dong-can-khon"),
            title = "Vũ Động Càn Khôn",
            coverUrl = "https://images.unsplash.com/photo-1512820790803-83ca734da794",
            authors = listOf(Author("2", "Thiên Tằm Thổ Đậu")),
            categories = listOf(Category("3", "Huyền Huyễn", "huyen-huyen")),
            status = StoryStatus.COMPLETED,
            origin = StoryOrigin.REMOTE,
            description = "Lâm Động xuất thân từ một phân gia của Lâm thị gia tộc tại Thanh Dương trấn.",
            totalChapters = 1307,
            latestChapterTitle = "Chương 1307: Đại kết cục",
            updatedAt = 1680000000000L
        ),
        Story(
            id = StoryId.create(ID, "toan-chuc-cao-thu"),
            title = "Toàn Chức Cao Thủ",
            coverUrl = "https://images.unsplash.com/photo-1516321318423-f06f85e504b3",
            authors = listOf(Author("3", "Hồ Điệp Lam")),
            categories = listOf(Category("5", "Võng Du", "vong-du"), Category("6", "Đô Thị", "do-thi")),
            status = StoryStatus.COMPLETED,
            origin = StoryOrigin.REMOTE,
            description = "Diệp Tu - cao thủ bậc nhất trong game Vinh Diệu bị câu lạc bộ xua đuổi, bắt đầu lại từ đầu tại một quán net.",
            totalChapters = 1728,
            latestChapterTitle = "Chương 1728: Vinh Quang không bao giờ tắt",
            updatedAt = 1670000000000L
        ),
        Story(
            id = StoryId.create(ID, "quy-bi-chi-chu"),
            title = "Quỷ Bí Chi Chủ",
            coverUrl = "https://images.unsplash.com/photo-1497633762265-9d179a990aa6",
            authors = listOf(Author("4", "Ái Tiềm Thủy Đích Ô Tặc")),
            categories = listOf(Category("3", "Huyền Huyễn", "huyen-huyen"), Category("7", "Trinh Thám", "trinh-tham")),
            status = StoryStatus.COMPLETED,
            origin = StoryOrigin.REMOTE,
            description = "Trong thủy triều của hơi nước cùng máy móc, ai có thể chạm tới phi phàm?",
            totalChapters = 1432,
            latestChapterTitle = "Chương 1432: Kẻ Khờ chìm vào giấc ngủ",
            updatedAt = 1710000000000L
        )
    )

    override suspend fun getHomeSections(): AppResult<List<StorySection>> {
        val sections = listOf(
            StorySection(
                id = "featured",
                title = "Truyện Nổi Bật",
                type = SectionType.FEATURED,
                stories = stories.take(3),
                hasMore = true
            ),
            StorySection(
                id = "popular",
                title = "Được Đọc Nhiều Nhất",
                type = SectionType.POPULAR,
                stories = stories.takeLast(3),
                hasMore = true
            ),
            StorySection(
                id = "completed",
                title = "Truyện Hoàn Thành",
                type = SectionType.COMPLETED,
                stories = stories.filter { it.status == StoryStatus.COMPLETED },
                hasMore = false
            )
        )
        return AppResult.Success(sections)
    }

    override suspend fun getLatestUpdates(page: Int): AppResult<StoryPage> {
        val pageSize = 10
        val startIndex = (page - 1) * pageSize
        val pageStories = stories.drop(startIndex).take(pageSize)
        val hasNextPage = startIndex + pageSize < stories.size
        return AppResult.Success(
            StoryPage(
                stories = pageStories,
                page = page,
                hasNextPage = hasNextPage,
                totalPages = (stories.size + pageSize - 1) / pageSize,
                totalResults = stories.size
            )
        )
    }

    override suspend fun getPopular(page: Int): AppResult<StoryPage> {
        return getLatestUpdates(page)
    }

    override suspend fun search(query: String, page: Int, filter: StoryFilter?): AppResult<StoryPage> {
        val trimmed = query.trim().lowercase()
        val filtered = stories.filter { story ->
            val matchesQuery = trimmed.isEmpty() || story.title.lowercase().contains(trimmed) ||
                    story.authors.any { it.name.lowercase().contains(trimmed) }
            val matchesCategory = filter?.category == null || story.categories.any { it.slug == filter.category || it.name.equals(filter.category, ignoreCase = true) }
            val matchesStatus = filter?.status == null || story.status == filter.status
            matchesQuery && matchesCategory && matchesStatus
        }

        val pageSize = 10
        val startIndex = (page - 1) * pageSize
        val pageStories = filtered.drop(startIndex).take(pageSize)
        val hasNext = startIndex + pageSize < filtered.size

        return AppResult.Success(
            StoryPage(
                stories = pageStories,
                page = page,
                hasNextPage = hasNext,
                totalPages = (filtered.size + pageSize - 1) / pageSize,
                totalResults = filtered.size
            )
        )
    }

    override suspend fun getStoryDetail(rawId: String): AppResult<StoryDetail> {
        val story = stories.firstOrNull { it.id.rawId == rawId }
            ?: return AppResult.Error(AppError.Source(message = "Story not found: $rawId", sourceId = ID))

        val detail = StoryDetail(
            story = story,
            sourceInfo = metadata.toStorySourceInfo(),
            fullDescription = story.description + "\n\nĐây là tác phẩm kinh điển được hàng triệu độc giả yêu thích.",
            rating = 4.9f,
            views = 9850000L,
            chapters = generateChapters(story.id, count = 20)
        )
        return AppResult.Success(detail)
    }

    override suspend fun getChapterList(rawId: String): AppResult<List<Chapter>> {
        val storyId = StoryId.create(ID, rawId)
        return AppResult.Success(generateChapters(storyId, count = 20))
    }

    override suspend fun getChapterContent(chapterId: String): AppResult<ChapterContent> {
        val parts = chapterId.split("_")
        val index = parts.lastOrNull()?.toIntOrNull() ?: 1
        val rawStoryId = parts.getOrNull(0) ?: "pham-nhan-tu-tien"
        val storyId = StoryId.create(ID, rawStoryId)

        val paragraphs = listOf(
            "Mặt trời lặn dần về hướng tây, nhuộm đỏ ráng chiều trên đỉnh núi.",
            "Tại một góc sân nhỏ, Hàn Lập đang chăm chú quan sát bình nhỏ màu xanh lục trong tay.",
            "Bình nhỏ phát ra ánh huỳnh quang mờ ảo, hấp thu linh khí thiên địa xung quanh từng chút một.",
            "Hắn biết, đây chính là cơ duyên lớn nhất trong đời giúp hắn bước chân vào con đường trường sinh bất lão.",
            "Hít sâu một hơi, hắn cẩn thận cất bình nhỏ vào trong ngực áo, tiếp tục khoanh chân tĩnh tọa tu luyện Trường Xuân Công."
        )

        val content = ChapterContent(
            chapterId = chapterId,
            storyId = storyId,
            title = "Chương $index: Tu Tiên Bí Mật",
            content = paragraphs.joinToString("\n\n"),
            paragraphs = paragraphs,
            wordCount = paragraphs.sumOf { it.length },
            sourceInfo = metadata.displayName
        )

        return AppResult.Success(content)
    }

    private fun generateChapters(storyId: StoryId, count: Int): List<Chapter> {
        return (1..count).map { idx ->
            Chapter(
                id = "${storyId.rawId}_$idx",
                storyId = storyId,
                index = idx,
                title = "Chương $idx: Khởi đầu hành trình",
                url = "${metadata.baseUrl}/story/${storyId.rawId}/chapter-$idx",
                isDownloaded = false,
                isRead = idx == 1,
                wordCount = 1500,
                publishedAt = 1700000000000L + (idx * 86400000L)
            )
        }
    }

    companion object {
        const val ID = "stub_source"
    }
}
