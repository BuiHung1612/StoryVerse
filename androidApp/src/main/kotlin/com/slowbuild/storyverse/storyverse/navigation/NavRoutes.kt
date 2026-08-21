package com.slowbuild.storyverse.storyverse.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.vector.ImageVector
import com.slowbuild.storyverse.domain.i18n.AppStringKey
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

// ─── Navigation route definitions ────────────────────────────────────────────

sealed class NavRoute(val route: String) {
    data object Home : NavRoute("home")
    data object Search : NavRoute("search")
    data object Library : NavRoute("library")
    data object Settings : NavRoute("settings")

    data object StoryDetail : NavRoute("story_detail/{storyId}") {
        fun createRoute(storyId: String): String {
            val encodedId = URLEncoder.encode(storyId, StandardCharsets.UTF_8.toString())
            return "story_detail/$encodedId"
        }

        fun parseStoryId(encodedId: String?): String {
            return if (encodedId != null) {
                URLDecoder.decode(encodedId, StandardCharsets.UTF_8.toString())
            } else ""
        }
    }

    data object Reader : NavRoute("reader/{storyId}/{chapterId}") {
        fun createRoute(storyId: String, chapterId: String): String {
            val encodedStoryId = URLEncoder.encode(storyId, StandardCharsets.UTF_8.toString())
            val encodedChapterId = URLEncoder.encode(chapterId, StandardCharsets.UTF_8.toString())
            return "reader/$encodedStoryId/$encodedChapterId"
        }

        fun parseArgs(encodedStoryId: String?, encodedChapterId: String?): Pair<String, String> {
            val sId = if (encodedStoryId != null) URLDecoder.decode(encodedStoryId, StandardCharsets.UTF_8.toString()) else ""
            val cId = if (encodedChapterId != null) URLDecoder.decode(encodedChapterId, StandardCharsets.UTF_8.toString()) else ""
            return Pair(sId, cId)
        }
    }
}

// ─── Bottom navigation items ──────────────────────────────────────────────────

data class BottomNavItem(
    val route: String,
    val labelKey: AppStringKey,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem(
        route = NavRoute.Home.route,
        labelKey = AppStringKey.TAB_DISCOVER,
        selectedIcon = Icons.Filled.Explore,
        unselectedIcon = Icons.Outlined.Explore
    ),
    BottomNavItem(
        route = NavRoute.Search.route,
        labelKey = AppStringKey.TAB_SEARCH,
        selectedIcon = Icons.Filled.Search,
        unselectedIcon = Icons.Outlined.Search
    ),
    BottomNavItem(
        route = NavRoute.Library.route,
        labelKey = AppStringKey.TAB_LIBRARY,
        selectedIcon = Icons.Filled.Book,
        unselectedIcon = Icons.Outlined.Book
    ),
    BottomNavItem(
        route = NavRoute.Settings.route,
        labelKey = AppStringKey.TAB_SETTINGS,
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings
    )
)
