package com.slowbuild.storyverse.domain.source

import com.slowbuild.storyverse.domain.model.StoryOrigin
import kotlinx.coroutines.flow.StateFlow

interface StorySourceRegistry {
    fun register(source: StorySource)
    
    fun unregister(sourceId: String)
    
    fun getSource(sourceId: String): StorySource?
    
    fun requireSource(sourceId: String): StorySource
    
    fun getAllSources(): List<StorySource>
    
    fun getSourcesByOrigin(origin: StoryOrigin): List<StorySource>
    
    fun getDefaultSource(): StorySource?
    
    fun observeSources(): StateFlow<List<StorySourceMetadata>>
}
