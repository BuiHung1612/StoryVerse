package com.slowbuild.storyverse.data.source

import com.slowbuild.storyverse.core.result.AppResult
import com.slowbuild.storyverse.data.network.client.safeApiCall
import com.slowbuild.storyverse.domain.source.StorySource
import com.slowbuild.storyverse.domain.source.StorySourceMetadata
import io.ktor.client.HttpClient

abstract class RemoteStorySource(
    protected val httpClient: HttpClient
) : StorySource {
    abstract override val metadata: StorySourceMetadata

    protected suspend fun <T> executeApi(
        retryCount: Int = 1,
        call: suspend HttpClient.() -> T
    ): AppResult<T> {
        return safeApiCall(retryCount = retryCount) {
            httpClient.call()
        }
    }
}
