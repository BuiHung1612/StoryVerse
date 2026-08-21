package com.slowbuild.storyverse.storyverse.ui.detail

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
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.slowbuild.storyverse.domain.model.Chapter
import com.slowbuild.storyverse.storyverse.theme.localizedString
import com.slowbuild.storyverse.storyverse.ui.common.ErrorView
import com.slowbuild.storyverse.storyverse.ui.common.LoadingView
import com.slowbuild.storyverse.storyverse.ui.common.StoryVerseTopBar
import org.koin.androidx.compose.koinViewModel

@Composable
fun StoryDetailScreen(
    storyId: String,
    onNavigateBack: () -> Unit,
    onReadChapter: (storyId: String, chapterId: String) -> Unit,
    viewModel: StoryDetailViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(storyId) {
        viewModel.loadStory(storyId)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        StoryVerseTopBar(
            title = uiState.story?.title ?: localizedString(AppStringKey.APP_NAME),
            canNavigateBack = true,
            onNavigateBack = onNavigateBack,
            actions = {
                if (uiState.story != null) {
                    IconButton(onClick = { viewModel.toggleLibrary() }) {
                        Icon(
                            imageVector = if (uiState.isInLibrary) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                            contentDescription = "Lưu vào tủ sách",
                            tint = if (uiState.isInLibrary) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        )

        when {
            uiState.isLoading && uiState.story == null -> {
                LoadingView(modifier = Modifier.fillMaxSize())
            }
        uiState.errorMessage != null && uiState.story == null -> {
            ErrorView(
                modifier = Modifier.fillMaxSize(),
                message = uiState.errorMessage ?: localizedString(AppStringKey.ERROR_UNKNOWN),
                onRetry = { viewModel.loadStory(storyId) }
            )
        }
        uiState.story != null -> {
            val story = uiState.story!!
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header: Cover + Info
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .width(110.dp)
                                .height(155.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!story.coverUrl.isNullOrBlank()) {
                                AsyncImage(
                                    model = story.coverUrl,
                                    contentDescription = story.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.AutoStories,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = story.title,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 22.sp
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "${localizedString(AppStringKey.DETAIL_AUTHOR)}: ${story.authorNames}",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "${localizedString(AppStringKey.DETAIL_STATUS)}: ${story.status.name}",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "${localizedString(AppStringKey.DETAIL_CHAPTERS)}: ${story.totalChapters.coerceAtLeast(uiState.chapters.size)}",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // Action Buttons
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        val firstChapterId = uiState.chapters.firstOrNull()?.id.orEmpty()
                        val progressChapterId = uiState.readingProgress?.lastReadChapterId

                        val targetChapterId = progressChapterId ?: firstChapterId
                        val isContinuing = progressChapterId != null

                        Button(
                            onClick = {
                                if (targetChapterId.isNotBlank()) {
                                    onReadChapter(story.id.value, targetChapterId)
                                }
                            },
                            modifier = Modifier.weight(1f),
                            enabled = uiState.chapters.isNotEmpty()
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isContinuing) {
                                    localizedString(AppStringKey.DETAIL_CONTINUE_READING)
                                } else {
                                    localizedString(AppStringKey.DETAIL_READ_NOW)
                                }
                            )
                        }

                        OutlinedButton(
                            onClick = { viewModel.toggleLibrary() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = if (uiState.isInLibrary) {
                                    localizedString(AppStringKey.DETAIL_IN_LIBRARY)
                                } else {
                                    localizedString(AppStringKey.DETAIL_ADD_LIBRARY)
                                }
                            )
                        }

                        // Download Action Button
                        val dl = uiState.downloadProgress
                        val isDownloading = dl != null && dl.status == com.slowbuild.storyverse.domain.model.DownloadStatus.DOWNLOADING

                        OutlinedButton(
                            onClick = { viewModel.startDownload() },
                            modifier = Modifier.weight(1f),
                            enabled = !uiState.isDownloaded && !isDownloading
                        ) {
                            if (isDownloading) {
                                androidx.compose.material3.CircularProgressIndicator(
                                    modifier = Modifier.size(14.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "${(dl!!.progress * 100).toInt()}%", fontSize = 12.sp)
                            } else if (uiState.isDownloaded) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "Đã tải", fontSize = 12.sp)
                            } else {
                                Icon(
                                    imageVector = Icons.Default.ArrowDownward,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "Tải về", fontSize = 12.sp)
                            }
                        }
                    }
                }

                // Categories / Tags
                if (story.categories.isNotEmpty()) {
                    item {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            story.categories.forEach { category ->
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Text(
                                        text = category.name,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Description
                if (!story.description.isNullOrBlank()) {
                    item {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = localizedString(AppStringKey.DETAIL_DESCRIPTION_TITLE),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = story.description ?: "",
                                    fontSize = 14.sp,
                                    lineHeight = 20.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Chapter List Header
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = localizedString(AppStringKey.DETAIL_CHAPTER_LIST_TITLE),
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${uiState.chapters.size} chương",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Chapter Items
                items(uiState.chapters) { chapter ->
                    ChapterItem(
                        chapter = chapter,
                        isCurrentReading = uiState.readingProgress?.lastReadChapterId == chapter.id,
                        onClick = { onReadChapter(story.id.value, chapter.id) }
                    )
                }
            }
        }
    }
}
}

@Composable
fun ChapterItem(
    chapter: Chapter,
    isCurrentReading: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrentReading) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = chapter.title,
                fontSize = 14.sp,
                fontWeight = if (isCurrentReading) FontWeight.Bold else FontWeight.Normal,
                color = if (isCurrentReading) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )

            if (isCurrentReading) {
                Text(
                    text = "Đang đọc",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
