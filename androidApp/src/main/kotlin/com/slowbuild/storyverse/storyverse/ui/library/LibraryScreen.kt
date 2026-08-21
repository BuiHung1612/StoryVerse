package com.slowbuild.storyverse.storyverse.ui.library

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.slowbuild.storyverse.domain.i18n.AppStringKey
import com.slowbuild.storyverse.domain.i18n.AppStrings
import com.slowbuild.storyverse.domain.model.HistoryEntry
import com.slowbuild.storyverse.domain.model.Story
import com.slowbuild.storyverse.storyverse.theme.localizedString
import com.slowbuild.storyverse.storyverse.ui.common.EmptyView
import com.slowbuild.storyverse.storyverse.ui.common.StoryRowItem
import com.slowbuild.storyverse.storyverse.ui.common.StoryVerseTopBar
import org.koin.androidx.compose.koinViewModel

@Composable
fun LibraryScreen(
    onStoryClick: (Story) -> Unit,
    onContinueRead: (storyId: String, chapterId: String) -> Unit,
    viewModel: LibraryViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        StoryVerseTopBar(
            title = localizedString(AppStringKey.LIBRARY_TITLE),
            actions = {
                if (uiState.selectedTab == LibraryTab.HISTORY && uiState.historyEntries.isNotEmpty()) {
                    IconButton(onClick = { viewModel.clearHistory() }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Xóa lịch sử",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        )

        // Tab Row
        TabRow(
            selectedTabIndex = uiState.selectedTab.ordinal,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            Tab(
                selected = uiState.selectedTab == LibraryTab.HISTORY,
                onClick = { viewModel.selectTab(LibraryTab.HISTORY) },
                text = {
                    Text(
                        text = localizedString(AppStringKey.LIBRARY_TAB_HISTORY),
                        fontWeight = if (uiState.selectedTab == LibraryTab.HISTORY) FontWeight.Bold else FontWeight.Normal
                    )
                }
            )
            Tab(
                selected = uiState.selectedTab == LibraryTab.BOOKSHELF,
                onClick = { viewModel.selectTab(LibraryTab.BOOKSHELF) },
                text = {
                    Text(
                        text = localizedString(AppStringKey.LIBRARY_TAB_FAVORITES),
                        fontWeight = if (uiState.selectedTab == LibraryTab.BOOKSHELF) FontWeight.Bold else FontWeight.Normal
                    )
                }
            )
        }

        // Tab Content
        when (uiState.selectedTab) {
            LibraryTab.HISTORY -> {
                if (uiState.historyEntries.isEmpty()) {
                    EmptyView(
                        title = localizedString(AppStringKey.LIBRARY_EMPTY_TITLE),
                        message = "Truyện bạn đã đọc sẽ xuất hiện tại đây"
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(uiState.historyEntries) { entry ->
                            HistoryRowItem(
                                entry = entry,
                                onStoryClick = { onStoryClick(entry.story) },
                                onContinueRead = {
                                    onContinueRead(entry.story.id.value, entry.lastReadChapterId)
                                }
                            )
                        }
                    }
                }
            }
            LibraryTab.BOOKSHELF -> {
                if (uiState.savedStories.isEmpty()) {
                    EmptyView(
                        title = localizedString(AppStringKey.LIBRARY_EMPTY_TITLE),
                        message = "Bạn chưa lưu truyện nào vào tủ sách"
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(uiState.savedStories) { story ->
                            StoryRowItem(
                                story = story,
                                onClick = { onStoryClick(story) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryRowItem(
    entry: HistoryEntry,
    onStoryClick: () -> Unit,
    onContinueRead: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onContinueRead),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Cover
            Box(
                modifier = Modifier
                    .width(65.dp)
                    .height(90.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable(onClick = onStoryClick),
                contentAlignment = Alignment.Center
            ) {
                if (!entry.story.coverUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = entry.story.coverUrl,
                        contentDescription = entry.story.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.AutoStories,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Details
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = entry.story.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Đọc gần nhất: ${entry.lastReadChapterTitle}",
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = entry.story.authorNames,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
