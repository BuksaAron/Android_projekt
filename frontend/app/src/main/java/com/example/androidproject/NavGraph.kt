package com.example.progr3ssapp.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.androidproject.HomeScreen
import com.example.androidproject.LoginScreen
import com.example.androidproject.RegisterScreen
import com.example.androidproject.ResetPasswordScreen
import com.example.androidproject.SplashScreen
import com.example.androidproject.ui.habit.AddHabitScreen
import com.example.androidproject.ui.profile.ProfileScreen
import com.example.androidproject.ui.schedule.CreateScheduleScreen
import com.example.androidproject.ui.schedule.EditScheduleScreen
import com.example.androidproject.ui.schedule.ScheduleDetailsScreen


sealed class Screen(val route: String, val title: String) {
    object Splash : Screen("splash", "Splash")
    object Login : Screen("login", "Login")
    object Home : Screen("home", "Home")
    object Profile : Screen("profile", "Profile")
    object Register : Screen("register", "Register")
    object ResetPassword : Screen("reset_password", "Reset password")
    object AddHabit : Screen("add_habit", "Add habit")
    object CreateSchedule : Screen("create_schedule", "New schedule")

    object ScheduleDetails : Screen("schedule_details/{scheduleId}", "Schedule details") {
        const val ARG_ID = "scheduleId"
        fun createRoute(id: Long) = "schedule_details/$id"
    }

    object EditSchedule : Screen("edit_schedule/{scheduleId}", "Edit schedule") {
        const val ARG_ID = "scheduleId"
        fun createRoute(id: Long) = "edit_schedule/$id"
    }
}

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute == Screen.Home.route || currentRoute == Screen.Profile.route

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                        label = { Text("Home") },
                        selected = currentRoute == Screen.Home.route,
                        onClick = {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                        label = { Text("Profile") },
                        selected = currentRoute == Screen.Profile.route,
                        onClick = {
                            navController.navigate(Screen.Profile.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Splash.route) {
                SplashScreen(
                    onNavigateToHome = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    },
                    onNavigateToLogin = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.Login.route) {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    onNavigateToRegister = {
                        navController.navigate(Screen.Register.route)
                    },
                    onNavigateToResetPassword = {
                        navController.navigate(Screen.ResetPassword.route)
                    }
                )
            }
            composable(Screen.Register.route) {
                RegisterScreen(
                    onRegisterSuccess = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onNavigateToLogin = {
                        navController.popBackStack()
                    }
                )
            }
            composable(Screen.ResetPassword.route) {
                ResetPasswordScreen(
                    onNavigateToLogin = {
                        navController.popBackStack()
                    }
                )
            }
            composable(Screen.Home.route) {
                HomeScreen(
                    onAddHabit = { navController.navigate(Screen.AddHabit.route) },
                    onCreateSchedule = { navController.navigate(Screen.CreateSchedule.route) },
                    onScheduleClick = { id ->
                        navController.navigate(Screen.ScheduleDetails.createRoute(id))
                    }
                )
            }
            composable(Screen.Profile.route) {
                ProfileScreen(
                    onLogout = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }
            composable(Screen.AddHabit.route) {
                AddHabitScreen(
                    onBack = { navController.popBackStack() },
                    onSaved = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    onNavigateToCreateSchedule = {
                        navController.navigate(Screen.CreateSchedule.route)
                    }
                )
            }

            composable(Screen.CreateSchedule.route) {
                CreateScheduleScreen(
                    onBack = { navController.popBackStack() },
                    onSaved = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    onNavigateToAddHabit = {
                        navController.navigate(Screen.AddHabit.route)
                    }
                )
            }

            composable(
                route = Screen.ScheduleDetails.route,
                arguments = listOf(navArgument(Screen.ScheduleDetails.ARG_ID) { type = NavType.LongType })
            ) { entry ->
                val scheduleId = entry.arguments?.getLong(Screen.ScheduleDetails.ARG_ID)
                    ?: return@composable
                ScheduleDetailsScreen(
                    scheduleId = scheduleId,
                    onBack = { navController.popBackStack() },
                    onEdit = { id -> navController.navigate(Screen.EditSchedule.createRoute(id)) },
                    onDeleted = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.EditSchedule.route,
                arguments = listOf(navArgument(Screen.EditSchedule.ARG_ID) { type = NavType.LongType })
            ) { entry ->
                val scheduleId = entry.arguments?.getLong(Screen.EditSchedule.ARG_ID)
                    ?: return@composable
                EditScheduleScreen(
                    scheduleId = scheduleId,
                    onBack = { navController.popBackStack() },
                    onSaved = { navController.popBackStack() }
                )
            }
        }
    }
}