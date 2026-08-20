package com.slowbuild.storyverse.data.source

import com.slowbuild.storyverse.core.logging.AppLogger
import com.slowbuild.storyverse.domain.model.StoryOrigin
import com.slowbuild.storyverse.domain.source.StorySource
import com.slowbuild.storyverse.domain.source.StorySourceMetadata
import com.slowbuild.storyverse.domain.source.StorySourceRegistry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class StorySourceRegistryImpl(
    initialSources: List<StorySource> = emptyList()
) : StorySourceRegistry {

    private val sourcesMap = mutableMapOf<String, StorySource>()
    private val mutex = Mutex()

    private val _sourcesState = MutableStateFlow<List<StorySourceMetadata>>(emptyList())
    override fun observeSources(): StateFlow<List<StorySourceMetadata>> = _sourcesState.asStateFlow()

    init {
        initialSources.forEach { register(it) }
    }

    override fun register(source: StorySource) {
        val id = source.metadata.id
        sourcesMap[id] = source
        updateState()
        AppLogger.i("StorySourceRegistry") { "Registered source: $id (${source.metadata.displayName})" }
    }

    override fun unregister(sourceId: String) {
        sourcesMap.remove(sourceId)
        updateState()
        AppLogger.i("StorySourceRegistry") { "Unregistered source: $sourceId" }
    }

    override fun getSource(sourceId: String): StorySource? {
        return sourcesMap[sourceId]
    }

    override fun requireSource(sourceId: String): StorySource {
        return sourcesMap[sourceId]
            ?: throw NoSuchElementException("StorySource with id '$sourceId' is not registered.")
    }

    override fun getAllSources(): List<StorySource> {
        return sourcesMap.values.toList()
    }

    override fun getSourcesByOrigin(origin: StoryOrigin): List<StorySource> {
        return sourcesMap.values.filter { it.metadata.origin == origin }
    }

    override fun getDefaultSource(): StorySource? {
        // Returns first enabled remote source, or first registered source
        return sourcesMap.values.firstOrNull { it.metadata.origin == StoryOrigin.REMOTE && it.metadata.isEnabled }
            ?: sourcesMap.values.firstOrNull()
    }

    private fun updateState() {
        _sourcesState.value = sourcesMap.values.map { it.metadata }
    }
}
