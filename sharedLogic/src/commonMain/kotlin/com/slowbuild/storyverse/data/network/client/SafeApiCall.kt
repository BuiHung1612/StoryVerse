package com.slowbuild.storyverse.data.network.client

import com.slowbuild.storyverse.core.logging.AppLogger
import com.slowbuild.storyverse.core.result.AppError
import com.slowbuild.storyverse.core.result.AppResult
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ResponseException
import io.ktor.client.plugins.ServerResponseException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.serialization.SerializationException

/**
 * Executes a network call with error mapping and optional retry for transient failures.
 *
 * @param retryCount Number of retries on transient errors (timeouts, 5xx server errors). Default 0 (no retry).
 * @param initialDelayMillis Initial delay before retry.
 * @param maxDelayMillis Maximum delay cap between retries.
 * @param factor Exponential backoff multiplier.
 * @param call Suspend block executing the HTTP request.
 */
suspend fun <T> safeApiCall(
    retryCount: Int = 0,
    initialDelayMillis: Long = 500,
    maxDelayMillis: Long = 3000,
    factor: Double = 2.0,
    call: suspend () -> T
): AppResult<T> {
    var currentDelay = initialDelayMillis
    var attemptsLeft = retryCount

    while (true) {
        try {
            val response = call()
            return AppResult.Success(response)
        } catch (e: CancellationException) {
            // Never swallow Coroutine Cancellation
            throw e
        } catch (e: ClientRequestException) {
            // 4xx Client Errors (do not retry)
            val statusCode = e.response.status.value
            val message = "Client error ($statusCode): ${e.message}"
            AppLogger.w("SafeApiCall", e) { message }
            return AppResult.Error(AppError.Network(message = message, statusCode = statusCode))
        } catch (e: ServerResponseException) {
            // 5xx Server Errors (transient, retryable)
            val statusCode = e.response.status.value
            val message = "Server error ($statusCode): ${e.message}"
            AppLogger.w("SafeApiCall", e) { message }
            if (attemptsLeft > 0) {
                attemptsLeft--
                AppLogger.i("SafeApiCall") { "Retrying after server error... attempts left: $attemptsLeft" }
                delay(currentDelay)
                currentDelay = (currentDelay * factor).toLong().coerceAtMost(maxDelayMillis)
                continue
            }
            return AppResult.Error(AppError.Network(message = message, statusCode = statusCode))
        } catch (e: HttpRequestTimeoutException) {
            // Timeout errors (retryable)
            val message = "Request timed out: ${e.message}"
            AppLogger.w("SafeApiCall", e) { message }
            if (attemptsLeft > 0) {
                attemptsLeft--
                AppLogger.i("SafeApiCall") { "Retrying after timeout... attempts left: $attemptsLeft" }
                delay(currentDelay)
                currentDelay = (currentDelay * factor).toLong().coerceAtMost(maxDelayMillis)
                continue
            }
            return AppResult.Error(AppError.Network(message = message, statusCode = 408))
        } catch (e: SerializationException) {
            // Response format mismatch (do not retry)
            val message = "Data parsing error: ${e.message}"
            AppLogger.e("SafeApiCall", e) { message }
            return AppResult.Error(AppError.Network(message = message))
        } catch (e: ResponseException) {
            val statusCode = e.response.status.value
            val message = "HTTP error ($statusCode): ${e.message}"
            AppLogger.w("SafeApiCall", e) { message }
            return AppResult.Error(AppError.Network(message = message, statusCode = statusCode))
        } catch (e: Exception) {
            val message = e.message ?: "Unknown network exception"
            AppLogger.e("SafeApiCall", e) { "Network error: $message" }
            if (attemptsLeft > 0) {
                attemptsLeft--
                AppLogger.i("SafeApiCall") { "Retrying after error: $message... attempts left: $attemptsLeft" }
                delay(currentDelay)
                currentDelay = (currentDelay * factor).toLong().coerceAtMost(maxDelayMillis)
                continue
            }
            return AppResult.Error(AppError.Network(message = message))
        }
    }
}
