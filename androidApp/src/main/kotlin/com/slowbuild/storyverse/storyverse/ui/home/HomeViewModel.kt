package com.slowbuild.storyverse.storyverse.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.slowbuild.storyverse.core.result.AppResult
import com.slowbuild.storyverse.domain.model.Story
import com.slowbuild.storyverse.domain.source.StorySourceRegistry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = true,
    val featuredStories: List<Story> = emptyList(),
    val latestStories: List<Story> = emptyList(),
    val topRatedStories: List<Story> = emptyList(),
    val errorMessage: String? = null
)

class HomeViewModel(
    private val storySourceRegistry: StorySourceRegistry
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadHomeData()
    }

    fun loadHomeData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val source = storySourceRegistry.getDefaultSource()
            if (source == null) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Không tìm thấy nguồn truyện khả dụng"
                    )
                }
                return@launch
            }

            when (val sectionsResult = source.getHomeSections()) {
                is AppResult.Success -> {
                    val sections = sectionsResult.data
                    val featured = sections.find { it.title.contains("Nổi bật", ignoreCase = true) || it.title.contains("Featured", ignoreCase = true) }?.stories
                        ?: sections.firstOrNull()?.stories.orEmpty()
                    val latest = sections.find { it.title.contains("Mới", ignoreCase = true) || it.title.contains("Latest", ignoreCase = true) }?.stories
                        ?: sections.getOrNull(1)?.stories.orEmpty()
                    val top = sections.find { it.title.contains("Đọc nhiều", ignoreCase = true) || it.title.contains("Popular", ignoreCase = true) }?.stories
                        ?: sections.getOrNull(2)?.stories.orEmpty()

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            featuredStories = featured,
                            latestStories = latest,
                            topRatedStories = top,
                            errorMessage = null
                        )
                    }
                }
                is AppResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = sectionsResult.error.message
                        )
                    }
                }
            }
        }
    }
}
