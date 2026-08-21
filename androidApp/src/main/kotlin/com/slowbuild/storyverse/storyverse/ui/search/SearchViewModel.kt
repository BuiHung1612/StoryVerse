package com.slowbuild.storyverse.storyverse.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.slowbuild.storyverse.core.result.AppResult
import com.slowbuild.storyverse.domain.model.Story
import com.slowbuild.storyverse.domain.source.StoryFilter
import com.slowbuild.storyverse.domain.source.StorySourceRegistry
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SearchUiState(
    val query: String = "",
    val isLoading: Boolean = false,
    val searchResults: List<Story> = emptyList(),
    val availableCategories: List<String> = listOf("Tiên Hiệp", "Kiếm Hiệp", "Huyền Huyễn", "Đô Thị", "Khoa Huyễn", "Lịch Sử", "Võng Du"),
    val selectedCategory: String? = null,
    val hasSearched: Boolean = false,
    val errorMessage: String? = null
)

class SearchViewModel(
    private val storySourceRegistry: StorySourceRegistry
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    fun onQueryChange(newQuery: String) {
        _uiState.update { it.copy(query = newQuery) }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(300) // Debounce
            executeSearch(query = newQuery, category = _uiState.value.selectedCategory)
        }
    }

    fun onCategorySelect(category: String?) {
        val nextCategory = if (_uiState.value.selectedCategory == category) null else category
        _uiState.update { it.copy(selectedCategory = nextCategory) }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            executeSearch(query = _uiState.value.query, category = nextCategory)
        }
    }

    private suspend fun executeSearch(query: String, category: String?) {
        if (query.isBlank() && category == null) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    searchResults = emptyList(),
                    hasSearched = false,
                    errorMessage = null
                )
            }
            return
        }

        _uiState.update { it.copy(isLoading = true, hasSearched = true, errorMessage = null) }
        val source = storySourceRegistry.getDefaultSource() ?: return

        val filter = if (category != null) StoryFilter(category = category) else null
        when (val result = source.search(query = query, page = 1, filter = filter)) {
            is AppResult.Success -> {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        searchResults = result.data.stories,
                        errorMessage = null
                    )
                }
            }
            is AppResult.Error -> {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = result.error.message
                    )
                }
            }
        }
    }
}
