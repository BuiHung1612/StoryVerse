package com.slowbuild.storyverse.storyverse.ui.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.slowbuild.storyverse.domain.i18n.AppStringKey
import com.slowbuild.storyverse.domain.i18n.AppStrings
import com.slowbuild.storyverse.domain.model.Story
import com.slowbuild.storyverse.storyverse.theme.localizedString
import com.slowbuild.storyverse.storyverse.ui.common.EmptyView
import com.slowbuild.storyverse.storyverse.ui.common.ErrorView
import com.slowbuild.storyverse.storyverse.ui.common.LoadingView
import com.slowbuild.storyverse.storyverse.ui.common.StoryRowItem
import com.slowbuild.storyverse.storyverse.ui.common.StoryVerseTopBar
import org.koin.androidx.compose.koinViewModel

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment

@Composable
fun SearchScreen(
    onStoryClick: (Story) -> Unit,
    viewModel: SearchViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        StoryVerseTopBar(
            title = localizedString(AppStringKey.TAB_SEARCH)
        )

        // Search Input
        OutlinedTextField(
            value = uiState.query,
            onValueChange = { viewModel.onQueryChange(it) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            placeholder = {
                Text(
                    text = localizedString(AppStringKey.SEARCH_HINT),
                    fontSize = 14.sp
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            trailingIcon = {
                if (uiState.query.isNotEmpty()) {
                    IconButton(onClick = { viewModel.onQueryChange("") }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
            )
        )

        // Category Filter Chips
        if (uiState.availableCategories.isNotEmpty()) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(vertical = 6.dp)
            ) {
                items(uiState.availableCategories) { category ->
                    val isSelected = uiState.selectedCategory == category
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.onCategorySelect(category) },
                        label = { Text(category, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Content Area
        when {
            uiState.isLoading -> {
                LoadingView()
            }
            uiState.errorMessage != null -> {
                ErrorView(
                    message = uiState.errorMessage ?: localizedString(AppStringKey.ERROR_UNKNOWN),
                    onRetry = { viewModel.onQueryChange(uiState.query) }
                )
            }
            uiState.hasSearched && uiState.searchResults.isEmpty() -> {
                EmptyView(
                    title = localizedString(AppStringKey.SEARCH_NO_RESULTS),
                    message = localizedString(AppStringKey.SEARCH_HINT)
                )
            }
            !uiState.hasSearched -> {
                EmptyView(
                    title = localizedString(AppStringKey.TAB_SEARCH),
                    message = localizedString(AppStringKey.SEARCH_HINT)
                )
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(uiState.searchResults) { story ->
                        StoryRowItem(
                            story = story,
                            onClick = { onStoryClick(story) }
                        )
                    }

                    if (uiState.hasNextPage) {
                        item {
                            LaunchedEffect(Unit) {
                                viewModel.loadNextPage()
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
