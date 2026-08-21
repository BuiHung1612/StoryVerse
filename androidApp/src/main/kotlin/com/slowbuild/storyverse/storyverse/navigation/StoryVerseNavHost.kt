package com.slowbuild.storyverse.storyverse.navigation

import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.slowbuild.storyverse.storyverse.theme.localizedString
import com.slowbuild.storyverse.storyverse.ui.common.StoryVerseTopBar
import com.slowbuild.storyverse.storyverse.ui.detail.StoryDetailScreen
import com.slowbuild.storyverse.storyverse.ui.home.HomeScreen
import com.slowbuild.storyverse.storyverse.ui.library.LibraryScreen
import com.slowbuild.storyverse.storyverse.ui.reader.ReaderScreen
import com.slowbuild.storyverse.storyverse.ui.search.SearchScreen
import com.slowbuild.storyverse.storyverse.ui.settings.SettingsScreen

// ─── Animation constants ───────────────────────────────────────────────────────

private const val ANIM_PUSH = 320
private const val ANIM_POP  = 280
private const val ANIM_TAB  = 200
private const val SLIDE_PCT = 0.25f  // partial slide for depth

private fun pushEnter()  = slideInHorizontally(animationSpec = tween(ANIM_PUSH, easing = EaseInOut), initialOffsetX = { (it * SLIDE_PCT).toInt() }) + fadeIn(tween(ANIM_PUSH, easing = EaseInOut))
private fun pushExit()   = slideOutHorizontally(animationSpec = tween(ANIM_PUSH, easing = EaseInOut), targetOffsetX = { -(it * SLIDE_PCT).toInt() }) + fadeOut(tween(ANIM_PUSH, easing = EaseInOut))
private fun popEnter()   = slideInHorizontally(animationSpec = tween(ANIM_POP,  easing = EaseInOut), initialOffsetX = { -(it * SLIDE_PCT).toInt() }) + fadeIn(tween(ANIM_POP,  easing = EaseInOut))
private fun popExit()    = slideOutHorizontally(animationSpec = tween(ANIM_POP,  easing = EaseInOut), targetOffsetX = { (it * SLIDE_PCT).toInt() }) + fadeOut(tween(ANIM_POP,  easing = EaseInOut))
private fun tabEnter()   = fadeIn(tween(ANIM_TAB, easing = EaseInOut))
private fun tabExit()    = fadeOut(tween(ANIM_TAB, easing = EaseInOut))

// ─── Root App Composable ──────────────────────────────────────────────────────

@Composable
fun StoryVerseApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val isTopLevel = bottomNavItems.any { it.route == currentRoute }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (isTopLevel) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.background,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    bottomNavItems.forEach { item ->
                        val isSelected = currentRoute == item.route
                        val label = localizedString(item.labelKey)
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
                                    contentDescription = label
                                )
                            },
                            label = {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor    = MaterialTheme.colorScheme.onPrimaryContainer,
                                selectedTextColor    = MaterialTheme.colorScheme.primary,
                                indicatorColor       = MaterialTheme.colorScheme.primaryContainer,
                                unselectedIconColor  = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor  = MaterialTheme.colorScheme.onSurfaceVariant
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
            modifier = Modifier.padding(innerPadding),
                enterTransition    = { tabEnter() },
                exitTransition     = { tabExit()  },
                popEnterTransition = { tabEnter() },
                popExitTransition  = { tabExit()  }
            ) {
                // ── Home ─────────────────────────────────────────────────────
                composable(NavRoute.Home.route) {
                    HomeScreen(
                        onStoryClick = { story ->
                            navController.navigate(NavRoute.StoryDetail.createRoute(story.id.value))
                        }
                    )
                }

                // ── Search ───────────────────────────────────────────────────
                composable(NavRoute.Search.route) {
                    SearchScreen(
                        onStoryClick = { story ->
                            navController.navigate(NavRoute.StoryDetail.createRoute(story.id.value))
                        }
                    )
                }

                // ── Library ──────────────────────────────────────────────────
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

                // ── Settings ─────────────────────────────────────────────────
                composable(NavRoute.Settings.route) {
                    SettingsScreen()
                }

                // ── Story Detail (push/pop) ───────────────────────────────────
                composable(
                    route = NavRoute.StoryDetail.route,
                    arguments = listOf(navArgument("storyId") { type = NavType.StringType }),
                    enterTransition    = { pushEnter() },
                    exitTransition     = { pushExit()  },
                    popEnterTransition = { popEnter()  },
                    popExitTransition  = { popExit()   }
                ) { backStackEntry ->
                    StoryDetailScreen(
                        storyId = NavRoute.StoryDetail.parseStoryId(
                            backStackEntry.arguments?.getString("storyId")
                        ),
                        onNavigateBack = { navController.popBackStack() },
                        onReadChapter  = { sId, cId ->
                            navController.navigate(NavRoute.Reader.createRoute(sId, cId))
                        }
                    )
                }

                // ── Reader (push/pop, no TopBar) ──────────────────────────────
                composable(
                    route = NavRoute.Reader.route,
                    arguments = listOf(
                        navArgument("storyId")   { type = NavType.StringType },
                        navArgument("chapterId") { type = NavType.StringType }
                    ),
                    enterTransition    = { pushEnter() },
                    exitTransition     = { pushExit()  },
                    popEnterTransition = { popEnter()  },
                    popExitTransition  = { popExit()   }
                ) { backStackEntry ->
                    val (storyId, chapterId) = NavRoute.Reader.parseArgs(
                        encodedStoryId   = backStackEntry.arguments?.getString("storyId"),
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
