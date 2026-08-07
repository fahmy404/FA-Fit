package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.GymScreen
import com.example.ui.screens.ChallengesScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.ProgressScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.AddMealScreen
import com.example.ui.theme.AppTheme
import com.example.ui.theme.BrandPurple

import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.viewmodels.MainViewModel
import androidx.compose.runtime.collectAsState
import com.google.firebase.FirebaseApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        enableEdgeToEdge()
        setContent {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                val viewModel: MainViewModel = viewModel()
                val state by viewModel.state.collectAsState()

                AppTheme(isDarkTheme = state.isDarkTheme) {
                    val navController = rememberNavController()
                    val currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
                    val startDest = if (currentUser != null) {
                        if (state.isProfileLoading) "loading"
                        else if (!state.isProfileSetup) "profileSetup"
                        else "main"
                    } else "login"

                    NavHost(navController = navController, startDestination = startDest) {
                        composable("loading") {
                            Box(
                                modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            }
                            LaunchedEffect(state.isProfileLoading) {
                                if (!state.isProfileLoading) {
                                    if (state.isProfileSetup) {
                                        navController.navigate("main") { popUpTo("loading") { inclusive = true } }
                                    } else {
                                        navController.navigate("profileSetup") { popUpTo("loading") { inclusive = true } }
                                    }
                                }
                            }
                        }
                        composable("login") {
                            LoginScreen(
                                onLoginSuccess = {
                                    // Reload all data fresh after login to avoid stale state
                                    viewModel.reloadAllData()
                                    navController.navigate("loading") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable("profileSetup") {
                            com.example.ui.screens.ProfileSetupScreen(onSetupComplete = {
                                viewModel.loadProfile()
                                navController.navigate("main") {
                                    popUpTo("profileSetup") { inclusive = true }
                                }
                            })
                        }
                        composable("main") {
                            MainApp(
                                viewModel = viewModel,
                                state = state,
                                onLogout = {
                                    viewModel.logout()
                                    navController.navigate("login") {
                                        popUpTo("main") { inclusive = true }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MainApp(
    viewModel: MainViewModel = viewModel(),
    state: com.example.ui.viewmodels.AppState,
    onLogout: () -> Unit = {}
) {
    val navController = rememberNavController()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination

            // Hide bottom bar on addMeal screen
            val showBottomBar = currentDestination?.route != "addMeal"

            if (showBottomBar) {
                NavigationBar(
                    modifier = Modifier.navigationBarsPadding(),
                    containerColor = Color(0xFF1E1E1F).copy(alpha = 0.8f),
                    tonalElevation = 0.dp
                ) {
                    NavigationBarItem(
                        selected = currentDestination?.hierarchy?.any { it.route == "dashboard" } == true,
                        onClick = {
                            navController.navigate("dashboard") {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(Icons.Default.Home, contentDescription = "الرئيسية") },
                        label = { Text("الرئيسية", style = MaterialTheme.typography.labelSmall) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.outline,
                            unselectedTextColor = MaterialTheme.colorScheme.outline,
                            indicatorColor = Color.Transparent
                        )
                    )

                    NavigationBarItem(
                        selected = currentDestination?.hierarchy?.any { it.route == "progress" } == true,
                        onClick = {
                            navController.navigate("progress") {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(Icons.Default.TrendingUp, contentDescription = "التقدم") },
                        label = { Text("التقدم", style = MaterialTheme.typography.labelSmall) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.outline,
                            unselectedTextColor = MaterialTheme.colorScheme.outline,
                            indicatorColor = Color.Transparent
                        )
                    )

                    // FAB: Add Meal
                    NavigationBarItem(
                        selected = false,
                        onClick = { navController.navigate("addMeal") },
                        icon = {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .shadow(15.dp, CircleShape, ambientColor = BrandPurple, spotColor = BrandPurple)
                                    .background(BrandPurple, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "إضافة", tint = Color.White)
                            }
                        },
                        label = { Text("إضافة", style = MaterialTheme.typography.labelSmall) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.outline,
                            indicatorColor = Color.Transparent
                        )
                    )

                    NavigationBarItem(
                        selected = currentDestination?.hierarchy?.any { it.route == "gym" } == true,
                        onClick = {
                            navController.navigate("gym") {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(Icons.Default.FitnessCenter, contentDescription = "الجيم") },
                        label = { Text("الجيم", style = MaterialTheme.typography.labelSmall) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.outline,
                            unselectedTextColor = MaterialTheme.colorScheme.outline,
                            indicatorColor = Color.Transparent
                        )
                    )

                    NavigationBarItem(
                        selected = currentDestination?.hierarchy?.any { it.route == "settings" } == true ||
                                currentDestination?.hierarchy?.any { it.route == "challenges" } == true,
                        onClick = {
                            navController.navigate("settings") {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(Icons.Default.Settings, contentDescription = "الإعدادات") },
                        label = { Text("الإعدادات", style = MaterialTheme.typography.labelSmall) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.outline,
                            unselectedTextColor = MaterialTheme.colorScheme.outline,
                            indicatorColor = Color.Transparent
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "dashboard",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("dashboard") {
                DashboardScreen(
                    state = state,
                    viewModel = viewModel,
                    onProfileClick = { navController.navigate("challenges") }
                )
            }
            composable("progress") { ProgressScreen(state = state) }
            composable("gym") {
                GymScreen(
                    viewModel = viewModel,
                    state = state
                )
            }
            composable("challenges") { ChallengesScreen(viewModel = viewModel, state = state) }
            composable("settings") {
                SettingsScreen(
                    viewModel = viewModel,
                    state = state,
                    onLogout = onLogout,
                    onNavigateToChallenges = { navController.navigate("challenges") }
                )
            }
            composable("addMeal") {
                AddMealScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
