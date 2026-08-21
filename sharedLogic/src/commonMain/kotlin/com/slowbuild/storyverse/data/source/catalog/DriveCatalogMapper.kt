package com.slowbuild.storyverse.data.source.catalog

import com.slowbuild.storyverse.domain.model.Author
import com.slowbuild.storyverse.domain.model.Category
import com.slowbuild.storyverse.domain.model.Chapter
import com.slowbuild.storyverse.domain.model.Story
import com.slowbuild.storyverse.domain.model.StoryDetail
import com.slowbuild.storyverse.domain.model.StoryId
import com.slowbuild.storyverse.domain.model.StoryOrigin
import com.slowbuild.storyverse.domain.model.StorySourceInfo
import com.slowbuild.storyverse.domain.model.StoryStatus

object DriveCatalogMapper {

    data class ParsedTitleAuthor(
        val title: String,
        val author: String?
    )

    fun parseFilename(filename: String): ParsedTitleAuthor {
        val clean = filename
            .removeSuffix(".epub")
            .removeSuffix(".EPUB")
            .trim()

        if (clean.contains(" - ")) {
            val lastDashIndex = clean.lastIndexOf(" - ")
            val titlePart = clean.substring(0, lastDashIndex).trim()
            val authorPart = clean.substring(lastDashIndex + 3).trim()
            return ParsedTitleAuthor(
                title = if (titlePart.isNotEmpty()) titlePart else clean,
                author = if (authorPart.isNotEmpty()) authorPart else null
            )
        }

        return ParsedTitleAuthor(title = clean, author = null)
    }

    fun toCategorySlug(name: String): String {
        return name.lowercase()
            .replace("đ", "d")
            .replace(" ", "-")
            .replace("[^a-z0-9-]".toRegex(), "")
    }

    fun toDomain(item: DriveCatalogItemDto, sourceId: String): Story {
        val parsed = parseFilename(item.name)
        val authors = if (parsed.author != null) {
            listOf(Author(id = null, name = parsed.author))
        } else {
            emptyList()
        }

        val categories = item.subjects.map { subject ->
            Category(
                id = null,
                name = subject,
                slug = toCategorySlug(subject)
            )
        }

        return Story(
            id = StoryId.create(sourceId = sourceId, rawId = item.id),
            title = parsed.title,
            coverUrl = null,
            authors = authors,
            categories = categories,
            status = StoryStatus.COMPLETED,
            origin = StoryOrigin.REMOTE,
            description = "Tác phẩm trong bộ sưu tập EPUB tổng hợp (${item.sizeBytes / 1024} KB).",
            totalChapters = 1,
            latestChapterTitle = "Bản đầy đủ (Full EPUB)",
            updatedAt = null
        )
    }

    fun toDetail(
        item: DriveCatalogItemDto,
        sourceInfo: StorySourceInfo,
        chapters: List<Chapter>
    ): StoryDetail {
        val domainStory = toDomain(item, sourceInfo.id)
        val parsed = parseFilename(item.name)

        val metadata = mutableMapOf<String, String>()
        if (item.downloadUrl != null) metadata["downloadUrl"] = item.downloadUrl
        metadata["sizeBytes"] = item.sizeBytes.toString()
        if (item.updatedAt != null) metadata["updatedAt"] = item.updatedAt

        return StoryDetail(
            story = domainStory,
            sourceInfo = sourceInfo,
            fullDescription = buildString {
                append("Tên tác phẩm: ${parsed.title}\n")
                if (parsed.author != null) append("Tác giả: ${parsed.author}\n")
                if (item.subjects.isNotEmpty()) append("Thể loại: ${item.subjects.joinToString(", ")}\n")
                append("Dung lượng: ${item.sizeBytes / 1024} KB\n")
                append("Định dạng: Sách điện tử EPUB hoàn chỉnh.\n\n")
                append("Truyện đã được tải sẵn toàn bộ nội dung trong kho lưu trữ sưu tầm.")
            },
            rating = 4.8f,
            views = 1000L + (item.index * 137L % 50000L),
            chapters = chapters,
            extraMetadata = metadata
        )
    }
}
