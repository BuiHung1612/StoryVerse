package com.slowbuild.storyverse.storyverse.ui.home

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import com.slowbuild.storyverse.domain.model.Story
import com.slowbuild.storyverse.storyverse.theme.localizedString
import com.slowbuild.storyverse.storyverse.ui.common.ErrorView
import com.slowbuild.storyverse.storyverse.ui.common.LoadingView
import com.slowbuild.storyverse.storyverse.ui.common.StoryCard
import com.slowbuild.storyverse.storyverse.ui.common.StoryRowItem
import com.slowbuild.storyverse.storyverse.ui.common.StoryVerseTopBar
import org.koin.androidx.compose.koinViewModel

@Composable
fun HomeScreen(
    onStoryClick: (Story) -> Unit,
    viewModel: HomeViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        StoryVerseTopBar(
            title = localizedString(AppStringKey.APP_NAME)
        )

        when {
            uiState.isLoading -> {
                LoadingView(modifier = Modifier.fillMaxSize())
            }
            uiState.errorMessage != null -> {
                ErrorView(
                    modifier = Modifier.fillMaxSize(),
                    message = uiState.errorMessage ?: localizedString(AppStringKey.ERROR_UNKNOWN),
                    onRetry = { viewModel.loadHomeData() }
                )
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                // Hero Featured Carousel
                if (uiState.featuredStories.isNotEmpty()) {
                    item {
                        FeaturedSection(
                            stories = uiState.featuredStories,
                            onStoryClick = onStoryClick
                        )
                    }
                }

                // Popular / Top Rated Horizontal Section
                if (uiState.topRatedStories.isNotEmpty()) {
                    item {
                        SectionHeader(title = localizedString(AppStringKey.SECTION_POPULAR))
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(uiState.topRatedStories) { story ->
                                StoryCard(
                                    story = story,
                                    onClick = { onStoryClick(story) }
                                )
                            }
                        }
                    }
                }

                // Latest Stories Vertical List
                if (uiState.latestStories.isNotEmpty()) {
                    item {
                        SectionHeader(title = localizedString(AppStringKey.SECTION_LATEST))
                    }
                    items(uiState.latestStories) { story ->
                        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
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
}

@Composable
fun FeaturedSection(
    stories: List<Story>,
    onStoryClick: (Story) -> Unit
) {
    val heroStory = stories.firstOrNull() ?: return
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable { onStoryClick(heroStory) },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(85.dp)
                    .height(120.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (!heroStory.coverUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = heroStory.coverUrl,
                        contentDescription = heroStory.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.AutoStories,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = localizedString(AppStringKey.SECTION_FEATURED),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = heroStory.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 20.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = heroStory.authorNames,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
