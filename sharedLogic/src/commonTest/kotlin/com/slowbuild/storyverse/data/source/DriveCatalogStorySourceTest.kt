package com.slowbuild.storyverse.data.source

import com.slowbuild.storyverse.data.network.client.HttpClientFactory
import com.slowbuild.storyverse.data.source.catalog.DriveCatalogItemDto
import com.slowbuild.storyverse.data.source.catalog.DriveCatalogMapper
import com.slowbuild.storyverse.data.source.catalog.DriveCatalogStorySource
import com.slowbuild.storyverse.domain.source.StoryFilter
import com.slowbuild.storyverse.domain.source.StorySort
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class DriveCatalogStorySourceTest {

    private val sampleItems = listOf(
        DriveCatalogItemDto(
            index = 0,
            id = "item-1",
            name = "Phàm Nhân Tu Tiên - Vong Ngữ.epub",
            title = "Phàm Nhân Tu Tiên - Vong Ngữ.epub",
            sizeBytes = 2500000L,
            updatedAt = "2026-07-23T09:33:07.000Z",
            downloadUrl = "https://drive.google.com/uc?export=download&id=item-1",
            subjects = listOf("Tiên Hiệp", "Huyền Huyễn")
        ),
        DriveCatalogItemDto(
            index = 1,
            id = "item-2",
            name = "Đấu Phá Thương Khung - Thiên Tằm Thổ Đậu.epub",
            title = "Đấu Phá Thương Khung - Thiên Tằm Thổ Đậu.epub",
            sizeBytes = 1800000L,
            updatedAt = "2026-07-23T09:33:07.000Z",
            downloadUrl = "https://drive.google.com/uc?export=download&id=item-2",
            subjects = listOf("Huyền Huyễn", "Dị Giới")
        ),
        DriveCatalogItemDto(
            index = 2,
            id = "item-3",
            name = "Đừng Tìm Nhà Đầu Tư Yêu Đương - superpanda.epub",
            title = "Đừng Tìm Nhà Đầu Tư Yêu Đương - superpanda.epub",
            sizeBytes = 1095582L,
            updatedAt = "2026-07-23T09:33:07.000Z",
            downloadUrl = "https://drive.google.com/uc?export=download&id=item-3",
            subjects = listOf("Đô Thị", "Khoa Huyễn")
        )
    )

    @Test
    fun mapper_parses_filename_and_extracts_author() {
        val parsed1 = DriveCatalogMapper.parseFilename("Phàm Nhân Tu Tiên - Vong Ngữ.epub")
        assertEquals("Phàm Nhân Tu Tiên", parsed1.title)
        assertEquals("Vong Ngữ", parsed1.author)

        val parsed2 = DriveCatalogMapper.parseFilename("Đại Chúa Tể.epub")
        assertEquals("Đại Chúa Tể", parsed2.title)
        assertEquals(null, parsed2.author)
    }

    @Test
    fun source_with_preloaded_items_returns_home_sections() = runTest {
        val mockEngine = MockEngine { respond("{}", HttpStatusCode.OK) }
        val httpClient = HttpClientFactory.create(engine = mockEngine, enableLogging = false)
        val source = DriveCatalogStorySource(
            httpClient = httpClient,
            preloadedItems = sampleItems
        )

        val sectionsResult = source.getHomeSections()
        assertTrue(sectionsResult.isSuccess)
        val sections = sectionsResult.getOrNull()
        assertNotNull(sections)
        assertTrue(sections.isNotEmpty())
        assertEquals("featured", sections[0].id)
        assertEquals(3, sections[0].stories.size)
    }

    @Test
    fun source_searches_by_keyword_and_category_filter() = runTest {
        val mockEngine = MockEngine { respond("{}", HttpStatusCode.OK) }
        val httpClient = HttpClientFactory.create(engine = mockEngine, enableLogging = false)
        val source = DriveCatalogStorySource(
            httpClient = httpClient,
            preloadedItems = sampleItems
        )

        // Search by keyword
        val searchResult = source.search("vong ngữ", page = 1)
        assertTrue(searchResult.isSuccess)
        val searchPage = searchResult.getOrNull()
        assertNotNull(searchPage)
        assertEquals(1, searchPage.stories.size)
        assertEquals("Phàm Nhân Tu Tiên", searchPage.stories[0].title)

        // Search by category
        val categoryResult = source.search("", page = 1, filter = StoryFilter(category = "Huyền Huyễn"))
        assertTrue(categoryResult.isSuccess)
        val categoryPage = categoryResult.getOrNull()
        assertNotNull(categoryPage)
        assertEquals(2, categoryPage.stories.size)
    }

    @Test
    fun source_fetches_remote_catalog_with_mock_engine_json() = runTest {
        val jsonPayload = """
            {
                "generatedAt": "2026-08-21T00:00:00.000Z",
                "totalItemsRead": 1,
                "items": [
                    {
                        "index": 0,
                        "id": "remote-story-1",
                        "name": "Toàn Chức Cao Thủ - Hồ Điệp Lam.epub",
                        "sizeBytes": 3200000,
                        "downloadUrl": "https://example.com/download/1",
                        "subjects": ["Võng Du", "Đô Thị"]
                    }
                ]
            }
        """.trimIndent()

        val mockEngine = MockEngine {
            respond(
                content = jsonPayload,
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val httpClient = HttpClientFactory.create(engine = mockEngine, enableLogging = false)
        val source = DriveCatalogStorySource(httpClient = httpClient)

        val detailResult = source.getStoryDetail("remote-story-1")
        assertTrue(detailResult.isSuccess)
        val detail = detailResult.getOrNull()
        assertNotNull(detail)
        assertEquals("Toàn Chức Cao Thủ", detail.story.title)
        assertEquals("Hồ Điệp Lam", detail.story.authorNames)
        assertEquals(1, detail.chapters.size)
    }
}
