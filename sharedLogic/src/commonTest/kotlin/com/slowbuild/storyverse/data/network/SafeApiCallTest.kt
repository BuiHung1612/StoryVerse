package com.slowbuild.storyverse.data.network

import com.slowbuild.storyverse.core.result.AppError
import com.slowbuild.storyverse.data.network.client.HttpClientFactory
import com.slowbuild.storyverse.data.network.client.safeApiCall
import io.ktor.client.call.body
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertTrue

class SafeApiCallTest {

    @Test
    fun safe_api_call_returns_success_on_200() = runTest {
        val mockEngine = MockEngine {
            respond(
                content = "OK Content",
                status = HttpStatusCode.OK
            )
        }
        val client = HttpClientFactory.create(engine = mockEngine, enableLogging = false)

        val result = safeApiCall {
            client.get("https://example.com").body<String>()
        }

        assertTrue(result.isSuccess)
        assertEquals("OK Content", result.getOrNull())
    }

    @Test
    fun safe_api_call_maps_404_error_correctly() = runTest {
        val mockEngine = MockEngine {
            respond(
                content = "Not Found",
                status = HttpStatusCode.NotFound
            )
        }
        val client = HttpClientFactory.create(engine = mockEngine, enableLogging = false)

        val result = safeApiCall {
            client.get("https://example.com/not-found").body<String>()
        }

        assertTrue(result.isError)
        val error = result.errorOrNull()
        assertIs<AppError.Network>(error)
        assertEquals(404, error.statusCode)
    }

    @Test
    fun safe_api_call_retries_and_succeeds_on_transient_500() = runTest {
        var attempts = 0
        val mockEngine = MockEngine {
            attempts++
            if (attempts == 1) {
                respond(content = "Server Error", status = HttpStatusCode.InternalServerError)
            } else {
                respond(content = "Success on retry", status = HttpStatusCode.OK)
            }
        }
        val client = HttpClientFactory.create(engine = mockEngine, enableLogging = false)

        val result = safeApiCall(retryCount = 2, initialDelayMillis = 10) {
            client.get("https://example.com/retry").body<String>()
        }

        assertTrue(result.isSuccess)
        assertEquals("Success on retry", result.getOrNull())
        assertEquals(2, attempts)
    }

    @Test
    fun safe_api_call_rethrows_cancellation_exception() = runTest {
        assertFailsWith<CancellationException> {
            safeApiCall {
                throw CancellationException("Job was cancelled")
            }
        }
    }
}
