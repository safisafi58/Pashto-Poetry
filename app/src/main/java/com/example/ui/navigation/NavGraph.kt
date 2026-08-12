package com.example.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
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
import com.example.ui.theme.PashtoGold
import com.example.ui.viewmodel.AuthViewModel
import com.example.ui.viewmodel.PoetryViewModel
import kotlinx.coroutines.launch

sealed class Screen(val route: String, val title: String? = null, val icon: androidx.compose.ui.graphics.vector.ImageVector? = null) {
    object Splash : Screen("splash")
    object Home : Screen("home", "کور پاڼه", Icons.Default.Home)
    object Favorites : Screen("favorites", "ساتل شوي", Icons.Default.Favorite)
    object AdminsList : Screen("admins_list", "اډمینان", Icons.Default.Shield)
    object PoemDetail : Screen("poem_detail/{poemId}") {
        fun createRoute(poemId: String) = "poem_detail/$poemId"
    }
    object PoetDetail : Screen("poet_detail/{poetId}") {
        fun createRoute(poetId: String) = "poet_detail/$poetId"
    }
}

@Composable
fun NavGraph(
    poetryViewModel: PoetryViewModel,
    authViewModel: AuthViewModel,
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomBarScreens = listOf(
        Screen.Home,
        Screen.Favorites,
        Screen.AdminsList
    )

    val currentUser by authViewModel.currentUser.collectAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var showAboutDialog by remember { mutableStateOf(false) }

    RtlLayout {
        ModalNavigationDrawer(
            drawerState = drawerState,
            gesturesEnabled = currentRoute in bottomBarScreens.map { it.route },
            drawerContent = {
                ModalDrawerSheet(
                    drawerContainerColor = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.width(300.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(24.dp)
                    ) {
                        Column {
                            Surface(
                                shape = CircleShape,
                                color = PashtoGold.copy(alpha = 0.25f),
                                modifier = Modifier.size(56.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.MenuBook,
                                        contentDescription = null,
                                        tint = PashtoGold,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "پښتو شعرونه",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "د پښتو بډایه شعرونو او غزلونو مجموعه",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    NavigationDrawerItem(
                        label = { Text("کور پاڼه", fontWeight = FontWeight.Bold) },
                        icon = { Icon(Icons.Default.Home, contentDescription = null) },
                        selected = currentRoute == Screen.Home.route,
                        onClick = {
                            scope.launch { drawerState.close() }
                            if (currentRoute != Screen.Home.route) {
                                navController.navigate(Screen.Home.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )

                    NavigationDrawerItem(
                        label = { Text("ساتل شوي شعرونه", fontWeight = FontWeight.Bold) },
                        icon = { Icon(Icons.Default.Favorite, contentDescription = null) },
                        selected = currentRoute == Screen.Favorites.route,
                        onClick = {
                            scope.launch { drawerState.close() }
                            if (currentRoute != Screen.Favorites.route) {
                                navController.navigate(Screen.Favorites.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )

                    NavigationDrawerItem(
                        label = { Text("د اډمینانو لیست", fontWeight = FontWeight.Bold) },
                        icon = { Icon(Icons.Default.Shield, contentDescription = null) },
                        selected = currentRoute == Screen.AdminsList.route,
                        onClick = {
                            scope.launch { drawerState.close() }
                            if (currentRoute != Screen.AdminsList.route) {
                                navController.navigate(Screen.AdminsList.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp))

                    NavigationDrawerItem(
                        label = { Text("د اپلیکیشن په اړه", fontWeight = FontWeight.Bold) },
                        icon = { Icon(Icons.Default.Info, contentDescription = null) },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            showAboutDialog = true
                        },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }
        ) {
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
                                        if (currentRoute != screen.route) {
                                            navController.navigate(screen.route) {
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
                            onOpenDrawer = { scope.launch { drawerState.open() } },
                            onAdminsClick = { navController.navigate(Screen.AdminsList.route) }
                        )
                    }

                    composable(Screen.Favorites.route) {
                        FavoritesScreen(
                            viewModel = poetryViewModel,
                            onPoemClick = { poemId -> navController.navigate(Screen.PoemDetail.createRoute(poemId)) }
                        )
                    }

                    composable(Screen.AdminsList.route) {
                        AdminsListScreen(viewModel = poetryViewModel)
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
                }
            }
        }

        if (showAboutDialog) {
            AlertDialog(
                onDismissRequest = { showAboutDialog = false },
                title = { Text("د پښتو شعرونو اپلیکیشن", fontWeight = FontWeight.Bold) },
                text = {
                    Text("دا اپلیکیشن د پښتو ژبې د اصیلو او غوره اشعارو د لټون، لوستلو او خوندي کولو لپاره وړاندې شوی دی.")
                },
                confirmButton = {
                    TextButton(onClick = { showAboutDialog = false }) {
                        Text("تایید")
                    }
                }
            )
        }
    }
}
