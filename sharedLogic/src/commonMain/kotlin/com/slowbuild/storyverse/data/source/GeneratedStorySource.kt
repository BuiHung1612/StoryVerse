package com.slowbuild.storyverse.data.source

import com.slowbuild.storyverse.domain.model.StoryOrigin
import com.slowbuild.storyverse.domain.source.SourceCapabilities
import com.slowbuild.storyverse.domain.source.StorySource
import com.slowbuild.storyverse.domain.source.StorySourceMetadata

abstract class GeneratedStorySource : StorySource {
    override val metadata: StorySourceMetadata = StorySourceMetadata(
        id = ID,
        name = "ai_generated",
        displayName = "AI Story Projects",
        origin = StoryOrigin.AI_GENERATED,
        capabilities = SourceCapabilities(
            supportsSearch = true,
            supportsCategories = true,
            supportsRanking = false,
            supportsLatestUpdates = true,
            supportsPagination = false,
            supportsOfflineDownload = false
        )
    )

    companion object {
        const val ID = "ai_generated"
    }
}
