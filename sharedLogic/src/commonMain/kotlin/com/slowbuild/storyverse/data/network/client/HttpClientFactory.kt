package com.slowbuild.storyverse.data.network.client

import com.slowbuild.storyverse.core.logging.AppLogger
import com.slowbuild.storyverse.data.network.engine.createPlatformHttpClientEngine
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

object HttpClientFactory {

    val defaultJson = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
        prettyPrint = false
        coerceInputValues = true
    }

    fun create(
        engine: HttpClientEngine = createPlatformHttpClientEngine(),
        json: Json = defaultJson,
        enableLogging: Boolean = true,
        connectTimeoutMillis: Long = 15_000,
        requestTimeoutMillis: Long = 30_000,
        socketTimeoutMillis: Long = 30_000
    ): HttpClient {
        return HttpClient(engine) {
            install(ContentNegotiation) {
                json(json)
            }

            install(HttpTimeout) {
                this.connectTimeoutMillis = connectTimeoutMillis
                this.requestTimeoutMillis = requestTimeoutMillis
                this.socketTimeoutMillis = socketTimeoutMillis
            }

            if (enableLogging) {
                install(Logging) {
                    level = LogLevel.INFO
                    logger = object : Logger {
                        override fun log(message: String) {
                            AppLogger.d("KtorClient") { message }
                        }
                    }
                }
            }

            expectSuccess = true

            defaultRequest {
                contentType(ContentType.Application.Json)
                headers.append(HttpHeaders.UserAgent, "StoryVerse/1.0 (Android/iOS)")
            }
        }
    }
}
