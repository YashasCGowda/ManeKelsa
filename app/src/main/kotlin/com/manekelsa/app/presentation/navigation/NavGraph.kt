package com.manekelsa.app.presentation.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.*
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.manekelsa.app.presentation.screens.admin.AreaAssignmentScreen
import com.manekelsa.app.presentation.screens.auth.LoginScreen
import com.manekelsa.app.presentation.screens.home.HomeScreen
import com.manekelsa.app.presentation.screens.jobs.JobsScreen
import com.manekelsa.app.presentation.screens.profile.KycVerificationScreen
import com.manekelsa.app.presentation.screens.profile.ProfileScreen
import com.manekelsa.app.presentation.screens.search.SearchScreen
import com.manekelsa.app.presentation.screens.settings.SettingsScreen
import com.manekelsa.app.presentation.theme.AppColors

sealed class Screen(val route: String) {
    object Login           : Screen("login")
    object Home            : Screen("home")
    object Search          : Screen("search")
    object Jobs            : Screen("jobs")
    object Settings        : Screen("settings")
    object Profile         : Screen("profile/{workerId}") {
        fun go(id: String) = "profile/$id"
    }
    object KycVerification : Screen("kyc_verification/{workerId}") {
        fun go(id: String) = "kyc_verification/$id"
    }
    object AreaAssignment  : Screen("area_assignment")
}

data class NavItem(val screen: Screen, val label: String, val filled: ImageVector, val outlined: ImageVector)

val navItems = listOf(
    NavItem(Screen.Home,     "Home",     Icons.Filled.Home,     Icons.Outlined.Home),
    NavItem(Screen.Search,   "Search",   Icons.Filled.Search,   Icons.Outlined.Search),
    NavItem(Screen.Jobs,     "Jobs",     Icons.Filled.Work,     Icons.Outlined.Work),
    NavItem(Screen.Settings, "Settings", Icons.Filled.Settings, Icons.Outlined.Settings)
)

val bottomRoutes = navItems.map { it.screen.route }.toSet()

@Composable
fun ManeKelsaNavGraph(startDestination: String = Screen.Login.route) {
    val navController = rememberNavController()
    val currentEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentEntry?.destination?.route
    val showBottom = currentRoute in bottomRoutes

    Scaffold(
        bottomBar = {
            if (showBottom) {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                    navItems.forEach { item ->
                        val sel = currentRoute == item.screen.route
                        NavigationBarItem(
                            selected = sel,
                            onClick = {
                                navController.navigate(item.screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(if (sel) item.filled else item.outlined, item.label) },
                            label = { Text(item.label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = AppColors.Saffron,
                                selectedTextColor = AppColors.Saffron,
                                indicatorColor = AppColors.Saffron.copy(alpha = 0.12f)
                            )
                        )
                    }
                }
            }
        }
    ) { inner ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(inner),
            enterTransition = {
                fadeIn(animationSpec = tween(250)) + slideInHorizontally(
                    animationSpec = tween(250),
                    initialOffsetX = { it }
                )
            },
            exitTransition = {
                fadeOut(animationSpec = tween(200)) + slideOutHorizontally(
                    animationSpec = tween(250),
                    targetOffsetX = { -it / 3 }
                )
            },
            popEnterTransition = {
                fadeIn(animationSpec = tween(250)) + slideInHorizontally(
                    animationSpec = tween(250),
                    initialOffsetX = { -it / 3 }
                )
            },
            popExitTransition = {
                fadeOut(animationSpec = tween(200)) + slideOutHorizontally(
                    animationSpec = tween(250),
                    targetOffsetX = { it }
                )
            }
        ) {
            composable(Screen.Login.route) {
                LoginScreen(onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                })
            }
            composable(Screen.Home.route) {
                HomeScreen(
                    onWorkerClick = { navController.navigate(Screen.Profile.go(it)) },
                    onSearchClick = { navController.navigate(Screen.Search.route) },
                    onNavigateToJobs = { navController.navigate(Screen.Jobs.route) },
                    onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
                )
            }
            composable(Screen.Search.route) {
                SearchScreen(
                    onWorkerClick = { navController.navigate(Screen.Profile.go(it)) },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Jobs.route) {
                JobsScreen(onBack = { navController.popBackStack() })
            }
            composable(Screen.Settings.route) {
                SettingsScreen(
                    onBack = { navController.popBackStack() },
                    onLoggedOut = {
                        navController.navigate(Screen.Login.route) { popUpTo(0) { inclusive = true } }
                    },
                    onAdminClick = {
                        navController.navigate(Screen.AreaAssignment.route)
                    }
                )
            }
            composable(
                route = Screen.Profile.route,
                arguments = listOf(navArgument("workerId") { type = NavType.StringType })
            ) { back ->
                val id = back.arguments?.getString("workerId") ?: return@composable
                ProfileScreen(
                    workerId = id, 
                    onBack = { navController.popBackStack() },
                    onVerifyClick = { navController.navigate(Screen.KycVerification.go(id)) }
                )
            }
            
            composable(
                route = Screen.KycVerification.route,
                arguments = listOf(navArgument("workerId") { type = NavType.StringType })
            ) { back ->
                val id = back.arguments?.getString("workerId") ?: return@composable
                KycVerificationScreen(
                    workerId = id,
                    onSuccess = { navController.popBackStack() },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.AreaAssignment.route) {
                AreaAssignmentScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}
