package com.slowbuild.storyverse.data.source.catalog

import com.slowbuild.storyverse.core.logging.AppLogger
import com.slowbuild.storyverse.core.result.AppError
import com.slowbuild.storyverse.core.result.AppResult
import com.slowbuild.storyverse.data.source.RemoteStorySource
import com.slowbuild.storyverse.domain.model.Chapter
import com.slowbuild.storyverse.domain.model.ChapterContent
import com.slowbuild.storyverse.domain.model.StoryDetail
import com.slowbuild.storyverse.domain.model.StoryId
import com.slowbuild.storyverse.domain.model.StoryOrigin
import com.slowbuild.storyverse.domain.source.SectionType
import com.slowbuild.storyverse.domain.source.SourceCapabilities
import com.slowbuild.storyverse.domain.source.StoryFilter
import com.slowbuild.storyverse.domain.source.StoryPage
import com.slowbuild.storyverse.domain.source.StorySection
import com.slowbuild.storyverse.domain.source.StorySort
import com.slowbuild.storyverse.domain.source.StorySourceMetadata
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class DriveCatalogStorySource(
    httpClient: HttpClient,
    private val catalogUrl: String = DEFAULT_CATALOG_URL,
    preloadedItems: List<DriveCatalogItemDto>? = null
) : RemoteStorySource(httpClient) {

    override val metadata: StorySourceMetadata = StorySourceMetadata(
        id = ID,
        name = "drive_catalog",
        displayName = "Kho Truyện Sưu Tầm (10.000+ Truyện)",
        baseUrl = "https://drive.google.com",
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

    private val mutex = Mutex()
    private var cachedItems: List<DriveCatalogItemDto>? = preloadedItems

    private suspend fun ensureCatalogLoaded(): AppResult<List<DriveCatalogItemDto>> {
        cachedItems?.let { return AppResult.Success(it) }

        return mutex.withLock {
            cachedItems?.let { return@withLock AppResult.Success(it) }

            AppLogger.i("DriveCatalog") { "Fetching remote story catalog from $catalogUrl" }
            val fetchResult = executeApi(retryCount = 2) {
                get(catalogUrl).body<DriveCatalogResponseDto>()
            }

            when (fetchResult) {
                is AppResult.Success -> {
                    val items = fetchResult.data.items
                    cachedItems = items
                    AppLogger.i("DriveCatalog") { "Successfully loaded ${items.size} stories into catalog" }
                    AppResult.Success(items)
                }
                is AppResult.Error -> {
                    AppLogger.e("DriveCatalog") { "Failed to load remote catalog: ${fetchResult.error.message}" }
                    AppResult.Error(fetchResult.error)
                }
            }
        }
    }

    override suspend fun getHomeSections(): AppResult<List<StorySection>> {
        val loadResult = ensureCatalogLoaded()
        if (loadResult is AppResult.Error) return loadResult

        val items = (loadResult as AppResult.Success).data
        val sourceId = metadata.id

        val tienHiep = items.filter { item ->
            item.subjects.any { it.contains("Tiên Hiệp", ignoreCase = true) || it.contains("Huyền Huyễn", ignoreCase = true) }
        }

        val doThi = items.filter { item ->
            item.subjects.any { it.contains("Đô Thị", ignoreCase = true) || it.contains("Khoa Huyễn", ignoreCase = true) }
        }

        val sections = listOf(
            StorySection(
                id = "featured",
                title = "Truyện Tuyển Chọn",
                type = SectionType.FEATURED,
                stories = items.take(10).map { DriveCatalogMapper.toDomain(it, sourceId) },
                hasMore = true
            ),
            StorySection(
                id = "tien_hiep",
                title = "Tiên Hiệp & Huyền Huyễn",
                type = SectionType.POPULAR,
                stories = tienHiep.take(10).map { DriveCatalogMapper.toDomain(it, sourceId) },
                hasMore = tienHiep.size > 10
            ),
            StorySection(
                id = "do_thi",
                title = "Đô Thị & Khoa Huyễn",
                type = SectionType.RECOMMENDED,
                stories = doThi.take(10).map { DriveCatalogMapper.toDomain(it, sourceId) },
                hasMore = doThi.size > 10
            ),
            StorySection(
                id = "latest",
                title = "Mới Cập Nhật",
                type = SectionType.LATEST,
                stories = items.takeLast(10).reversed().map { DriveCatalogMapper.toDomain(it, sourceId) },
                hasMore = items.size > 10
            )
        )

        return AppResult.Success(sections)
    }

    override suspend fun getLatestUpdates(page: Int): AppResult<StoryPage> {
        return search(query = "", page = page, filter = StoryFilter(sort = StorySort.LATEST))
    }

    override suspend fun getPopular(page: Int): AppResult<StoryPage> {
        return search(query = "", page = page, filter = StoryFilter(sort = StorySort.POPULAR))
    }

    override suspend fun search(query: String, page: Int, filter: StoryFilter?): AppResult<StoryPage> {
        val loadResult = ensureCatalogLoaded()
        if (loadResult is AppResult.Error) return loadResult

        val allItems = (loadResult as AppResult.Success).data
        val trimmedQuery = query.trim().lowercase()

        var filtered = allItems.filter { item ->
            val matchesQuery = if (trimmedQuery.isEmpty()) {
                true
            } else {
                item.name.lowercase().contains(trimmedQuery) ||
                        item.subjects.any { it.lowercase().contains(trimmedQuery) }
            }

            val matchesCategory = if (filter?.category != null && filter.category.isNotBlank()) {
                val catQuery = filter.category.lowercase()
                item.subjects.any { subject ->
                    subject.lowercase() == catQuery ||
                            DriveCatalogMapper.toCategorySlug(subject) == catQuery
                }
            } else {
                true
            }

            matchesQuery && matchesCategory
        }

        // Sorting
        filtered = when (filter?.sort) {
            StorySort.LATEST -> filtered.reversed()
            StorySort.ALPHABETICAL -> filtered.sortedBy { it.name }
            else -> filtered
        }

        val pageSize = 20
        val startIndex = ((page - 1) * pageSize).coerceAtLeast(0)
        val pagedItems = filtered.drop(startIndex).take(pageSize)
        val hasNext = startIndex + pageSize < filtered.size

        val domainStories = pagedItems.map { DriveCatalogMapper.toDomain(it, metadata.id) }

        return AppResult.Success(
            StoryPage(
                stories = domainStories,
                page = page,
                hasNextPage = hasNext,
                totalPages = (filtered.size + pageSize - 1) / pageSize,
                totalResults = filtered.size
            )
        )
    }

    override suspend fun getStoryDetail(rawId: String): AppResult<StoryDetail> {
        val loadResult = ensureCatalogLoaded()
        if (loadResult is AppResult.Error) return loadResult

        val items = (loadResult as AppResult.Success).data
        val item = items.firstOrNull { it.id == rawId }
            ?: return AppResult.Error(AppError.Source("Không tìm thấy truyện trong kho sưu tầm (ID: $rawId)", sourceId = metadata.id))

        val storyId = StoryId.create(metadata.id, item.id)
        val chapters = listOf(
            Chapter(
                id = "${item.id}_full",
                storyId = storyId,
                index = 1,
                title = "Toàn Tập (Bản Đầy Đủ)",
                url = item.downloadUrl,
                isDownloaded = false,
                isRead = false,
                wordCount = (item.sizeBytes / 3).toInt().coerceAtLeast(1000),
                publishedAt = null
            )
        )

        val detail = DriveCatalogMapper.toDetail(
            item = item,
            sourceInfo = metadata.toStorySourceInfo(),
            chapters = chapters
        )

        return AppResult.Success(detail)
    }

    override suspend fun getChapterList(rawId: String): AppResult<List<Chapter>> {
        val detailResult = getStoryDetail(rawId)
        return when (detailResult) {
            is AppResult.Success -> AppResult.Success(detailResult.data.chapters)
            is AppResult.Error -> AppResult.Error(detailResult.error)
        }
    }

    override suspend fun getChapterContent(chapterId: String): AppResult<ChapterContent> {
        val rawStoryId = chapterId.substringBefore("_")
        val detailResult = getStoryDetail(rawStoryId)
        if (detailResult is AppResult.Error) return detailResult

        val detail = (detailResult as AppResult.Success).data
        val storyId = detail.story.id

        val paragraphs = listOf(
            "Tác phẩm: ${detail.story.title}",
            "Tác giả: ${detail.story.authorNames}",
            "Thể loại: ${detail.story.categories.joinToString { it.name }}",
            "Trạng thái: Hoàn thành",
            "---",
            "Nội dung toàn bộ tác phẩm đã được đóng gói hoàn chỉnh trong tệp sách điện tử EPUB.",
            "Bạn có thể mở đọc trực tiếp hoặc tải về máy để đọc offline bất cứ lúc nào."
        )

        val content = ChapterContent(
            chapterId = chapterId,
            storyId = storyId,
            title = "Toàn Tập (Bản Đầy Đủ)",
            content = paragraphs.joinToString("\n\n"),
            paragraphs = paragraphs,
            wordCount = paragraphs.sumOf { it.length },
            sourceInfo = metadata.displayName
        )

        return AppResult.Success(content)
    }

    companion object {
        const val ID = "drive_catalog"
        const val DEFAULT_CATALOG_URL = "https://drive.usercontent.google.com/download?id=1DWHJG0sfKvcuSi-ElUgOHWm84oa7O1Ob&export=download&confirm=t"
    }
}
