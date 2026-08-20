package com.slowbuild.storyverse.data.source

import com.slowbuild.storyverse.domain.model.StoryOrigin
import com.slowbuild.storyverse.domain.source.SourceCapabilities
import com.slowbuild.storyverse.domain.source.StorySource
import com.slowbuild.storyverse.domain.source.StorySourceMetadata

abstract class LocalStorySource : StorySource {
    override val metadata: StorySourceMetadata = StorySourceMetadata(
        id = ID,
        name = "local_epub",
        displayName = "Local & EPUB Books",
        origin = StoryOrigin.LOCAL_EPUB,
        capabilities = SourceCapabilities(
            supportsSearch = true,
            supportsCategories = false,
            supportsRanking = false,
            supportsLatestUpdates = true,
            supportsPagination = false,
            supportsOfflineDownload = false
        )
    )

    companion object {
        const val ID = "local_epub"
    }
}
