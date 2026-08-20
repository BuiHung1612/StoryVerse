package com.slowbuild.storyverse.data.network

import com.slowbuild.storyverse.data.network.client.HttpClientFactory
import com.slowbuild.storyverse.data.network.dto.ApiResponse
import com.slowbuild.storyverse.data.network.dto.StoryDto
import io.ktor.client.call.body
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.get
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class HttpClientFactoryTest {

    @Test
    fun client_deserializes_json_response_with_mock_engine() = runTest {
        val mockEngine = MockEngine { request ->
            respond(
                content = """
                    {
                        "success": true,
                        "data": {
                            "id": "pham-nhan",
                            "title": "Phàm Nhân Tu Tiên",
                            "total_chapters": 2446
                        }
                    }
                """.trimIndent(),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val client = HttpClientFactory.create(engine = mockEngine, enableLogging = false)
        val response = client.get("https://api.storyverse.com/story/pham-nhan").body<ApiResponse<StoryDto>>()

        assertTrue(response.success)
        val data = response.data
        assertNotNull(data)
        assertEquals("pham-nhan", data.id)
        assertEquals("Phàm Nhân Tu Tiên", data.title)
        assertEquals(2446, data.totalChapters)
    }
}
