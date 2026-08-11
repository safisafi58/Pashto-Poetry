package com.example.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.components.RtlLayout
import com.example.ui.screens.*
import com.example.ui.viewmodel.AdminViewModel
import com.example.ui.viewmodel.AuthViewModel
import com.example.ui.viewmodel.PoetryViewModel

sealed class Screen(val route: String, val title: String? = null, val icon: androidx.compose.ui.graphics.vector.ImageVector? = null) {
    object Splash : Screen("splash")
    object Home : Screen("home", "کور پاڼه", Icons.Default.Home)
    object Favorites : Screen("favorites", "ساتل شوي", Icons.Default.Favorite)
    object AddPoem : Screen("add_poem", "شعر لیکل", Icons.Default.AddCircle)
    object Profile : Screen("profile", "حساب", Icons.Default.Person)
    object PoemDetail : Screen("poem_detail/{poemId}") {
        fun createRoute(poemId: String) = "poem_detail/$poemId"
    }
    object PoetDetail : Screen("poet_detail/{poetId}") {
        fun createRoute(poetId: String) = "poet_detail/$poetId"
    }
    object AdminDashboard : Screen("admin_dashboard")
}

@Composable
fun NavGraph(
    poetryViewModel: PoetryViewModel,
    authViewModel: AuthViewModel,
    adminViewModel: AdminViewModel,
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomBarScreens = listOf(
        Screen.Home,
        Screen.Favorites,
        Screen.AddPoem,
        Screen.Profile
    )

    val currentUser by authViewModel.currentUser.collectAsState()

    RtlLayout {
        Scaffold(
            bottomBar = {
                if (currentRoute in bottomBarScreens.map { it.route }) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 6.dp
                    ) {
                        bottomBarScreens.forEach { screen ->
                            val selected = currentRoute == screen.route
                            NavigationBarItem(
                                selected = selected,
                                onClick = {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = {
                                    Icon(
                                        imageVector = screen.icon!!,
                                        contentDescription = screen.title
                                    )
                                },
                                label = {
                                    Text(
                                        text = screen.title ?: "",
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                modifier = Modifier.testTag("nav_${screen.route}")
                            )
                        }
                    }
                }
            }
        ) { paddingValues ->
            NavHost(
                navController = navController,
                startDestination = Screen.Splash.route,
                modifier = Modifier.padding(paddingValues)
            ) {
                composable(Screen.Splash.route) {
                    SplashScreen(
                        onSplashFinished = {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Splash.route) { inclusive = true }
                            }
                        }
                    )
                }

                composable(Screen.Home.route) {
                    HomeScreen(
                        viewModel = poetryViewModel,
                        onPoemClick = { poemId -> navController.navigate(Screen.PoemDetail.createRoute(poemId)) },
                        onPoetClick = { poetId -> navController.navigate(Screen.PoetDetail.createRoute(poetId)) },
                        onAddPoemClick = { navController.navigate(Screen.AddPoem.route) },
                        onAdminDashboardClick = { navController.navigate(Screen.AdminDashboard.route) },
                        onProfileClick = { navController.navigate(Screen.Profile.route) }
                    )
                }

                composable(Screen.Favorites.route) {
                    FavoritesScreen(
                        viewModel = poetryViewModel,
                        onPoemClick = { poemId -> navController.navigate(Screen.PoemDetail.createRoute(poemId)) }
                    )
                }

                composable(Screen.AddPoem.route) {
                    AddEditPoemScreen(
                        viewModel = poetryViewModel,
                        authorName = currentUser.name,
                        isAdmin = currentUser.isAdmin,
                        onBackClick = { navController.popBackStack() }
                    )
                }

                composable(Screen.Profile.route) {
                    ProfileAuthScreen(
                        authViewModel = authViewModel,
                        poetryViewModel = poetryViewModel,
                        onPoemClick = { poemId -> navController.navigate(Screen.PoemDetail.createRoute(poemId)) },
                        onAdminDashboardClick = { navController.navigate(Screen.AdminDashboard.route) }
                    )
                }

                composable(
                    route = Screen.PoemDetail.route,
                    arguments = listOf(navArgument("poemId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val poemId = backStackEntry.arguments?.getString("poemId") ?: ""
                    PoemDetailScreen(
                        poemId = poemId,
                        viewModel = poetryViewModel,
                        userName = currentUser.name,
                        onBackClick = { navController.popBackStack() }
                    )
                }

                composable(
                    route = Screen.PoetDetail.route,
                    arguments = listOf(navArgument("poetId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val poetId = backStackEntry.arguments?.getString("poetId") ?: ""
                    PoetDetailScreen(
                        poetId = poetId,
                        viewModel = poetryViewModel,
                        onPoemClick = { poemId -> navController.navigate(Screen.PoemDetail.createRoute(poemId)) },
                        onBackClick = { navController.popBackStack() }
                    )
                }

                composable(Screen.AdminDashboard.route) {
                    AdminDashboardScreen(
                        adminViewModel = adminViewModel,
                        onBackClick = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}
