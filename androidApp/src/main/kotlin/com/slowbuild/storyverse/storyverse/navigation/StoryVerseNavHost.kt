package com.slowbuild.storyverse.storyverse.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.slowbuild.storyverse.domain.i18n.AppStrings
import com.slowbuild.storyverse.storyverse.ui.detail.StoryDetailScreen
import com.slowbuild.storyverse.storyverse.ui.home.HomeScreen
import com.slowbuild.storyverse.storyverse.ui.library.LibraryScreen
import com.slowbuild.storyverse.storyverse.ui.reader.ReaderScreen
import com.slowbuild.storyverse.storyverse.ui.search.SearchScreen
import com.slowbuild.storyverse.storyverse.ui.settings.SettingsScreen

@Composable
fun StoryVerseApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val isTopLevelDestination = bottomNavItems.any { it.route == currentRoute }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (isTopLevelDestination) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    bottomNavItems.forEach { item ->
                        val isSelected = currentRoute == item.route
                        val labelText = com.slowbuild.storyverse.storyverse.theme.localizedString(item.labelKey)
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                if (currentRoute != item.route) {
                                    navController.navigate(item.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = labelText
                                )
                            },
                            label = {
                                Text(
                                    text = labelText,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = NavRoute.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // Tab: Home
            composable(NavRoute.Home.route) {
                HomeScreen(
                    onStoryClick = { story ->
                        navController.navigate(NavRoute.StoryDetail.createRoute(story.id.value))
                    }
                )
            }

            // Tab: Search
            composable(NavRoute.Search.route) {
                SearchScreen(
                    onStoryClick = { story ->
                        navController.navigate(NavRoute.StoryDetail.createRoute(story.id.value))
                    }
                )
            }

            // Tab: Library
            composable(NavRoute.Library.route) {
                LibraryScreen(
                    onStoryClick = { story ->
                        navController.navigate(NavRoute.StoryDetail.createRoute(story.id.value))
                    },
                    onContinueRead = { storyId, chapterId ->
                        navController.navigate(NavRoute.Reader.createRoute(storyId, chapterId))
                    }
                )
            }

            // Tab: Settings
            composable(NavRoute.Settings.route) {
                SettingsScreen()
            }

            // Screen: Story Detail
            composable(
                route = NavRoute.StoryDetail.route,
                arguments = listOf(
                    navArgument("storyId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val encodedStoryId = backStackEntry.arguments?.getString("storyId")
                val storyId = NavRoute.StoryDetail.parseStoryId(encodedStoryId)

                StoryDetailScreen(
                    storyId = storyId,
                    onNavigateBack = { navController.popBackStack() },
                    onReadChapter = { sId, cId ->
                        navController.navigate(NavRoute.Reader.createRoute(sId, cId))
                    }
                )
            }

            // Screen: Reader
            composable(
                route = NavRoute.Reader.route,
                arguments = listOf(
                    navArgument("storyId") { type = NavType.StringType },
                    navArgument("chapterId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val (storyId, chapterId) = NavRoute.Reader.parseArgs(
                    encodedStoryId = backStackEntry.arguments?.getString("storyId"),
                    encodedChapterId = backStackEntry.arguments?.getString("chapterId")
                )

                ReaderScreen(
                    storyId = storyId,
                    chapterId = chapterId,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateChapter = { nextChapterId ->
                        navController.navigate(NavRoute.Reader.createRoute(storyId, nextChapterId)) {
                            popUpTo(NavRoute.Reader.route) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}
