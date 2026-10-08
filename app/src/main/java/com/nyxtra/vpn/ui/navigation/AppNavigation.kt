package com.nyxtra.vpn.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.nyxtra.vpn.ui.components.NyxtraDrawer
import com.nyxtra.vpn.ui.screens.dashboard.DashboardScreen
import com.nyxtra.vpn.ui.screens.dashboard.DashboardViewModel
import com.nyxtra.vpn.ui.screens.logs.LogsScreen
import com.nyxtra.vpn.ui.screens.logs.LogsViewModel
import com.nyxtra.vpn.ui.screens.perapp.PerAppProxyScreen
import com.nyxtra.vpn.ui.screens.perapp.PerAppProxyViewModel
import com.nyxtra.vpn.ui.screens.profiles.ProfileEditScreen
import com.nyxtra.vpn.ui.screens.profiles.ProfilesViewModel
import com.nyxtra.vpn.ui.screens.settings.SettingsScreen
import com.nyxtra.vpn.ui.screens.settings.SettingsViewModel
import com.nyxtra.vpn.ui.theme.CanvasBg
import kotlinx.coroutines.launch

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Shared ViewModels
    val dashboardViewModel: DashboardViewModel = viewModel()
    val profilesViewModel: ProfilesViewModel = viewModel()
    val perAppViewModel: PerAppProxyViewModel = viewModel()
    val logsViewModel: LogsViewModel = viewModel()
    val settingsViewModel: SettingsViewModel = viewModel()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            NyxtraDrawer(
                currentRoute = currentRoute,
                onNavigate = { route ->
                    scope.launch { drawerState.close() }
                    if (currentRoute != route) {
                        navController.navigate(route) {
                            popUpTo(Screen.Dashboard.route) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }
    ) {
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier
                .fillMaxSize()
                .background(CanvasBg)
        ) {
            // Main Dashboard (Profile list + Floating Connect Button)
            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    dashboardViewModel = dashboardViewModel,
                    profilesViewModel = profilesViewModel,
                    onOpenDrawer = { scope.launch { drawerState.open() } },
                    onNavigateToEdit = { navController.navigate(Screen.ProfileEdit.route) },
                    onNavigateToLogs = { navController.navigate(Screen.Logs.route) },
                    onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
                )
            }

            composable(Screen.ProfileEdit.route) {
                ProfileEditScreen(
                    viewModel = profilesViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.PerApp.route) {
                PerAppProxyScreen(
                    viewModel = perAppViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Logs.route) {
                LogsScreen(
                    viewModel = logsViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Settings.route) {
                SettingsScreen(
                    viewModel = settingsViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
