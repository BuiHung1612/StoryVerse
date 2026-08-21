package com.slowbuild.storyverse.storyverse.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.slowbuild.storyverse.data.local.room.dao.StoryDao
import com.slowbuild.storyverse.domain.model.HistoryEntry
import com.slowbuild.storyverse.domain.model.Story
import com.slowbuild.storyverse.domain.repository.ReaderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class LibraryTab {
    HISTORY,
    BOOKSHELF
}

data class LibraryUiState(
    val selectedTab: LibraryTab = LibraryTab.HISTORY,
    val historyEntries: List<HistoryEntry> = emptyList(),
    val savedStories: List<Story> = emptyList(),
    val isLoading: Boolean = false
)

class LibraryViewModel(
    private val readerRepository: ReaderRepository,
    private val storyDao: StoryDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(LibraryUiState())
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    init {
        observeHistory()
        observeLibraryStories()
    }

    fun selectTab(tab: LibraryTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    private fun observeHistory() {
        viewModelScope.launch {
            readerRepository.observeHistory(limit = 50).collect { entries ->
                _uiState.update { it.copy(historyEntries = entries) }
            }
        }
    }

    private fun observeLibraryStories() {
        viewModelScope.launch {
            storyDao.observeLibraryStories().map { entities ->
                entities.map { it.toDomain() }
            }.collect { stories ->
                _uiState.update { it.copy(savedStories = stories) }
            }
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            readerRepository.clearHistory()
        }
    }
}
