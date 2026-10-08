package com.nyxtra.vpn.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.nyxtra.vpn.ui.screens.dashboard.DashboardScreen
import com.nyxtra.vpn.ui.screens.dashboard.DashboardViewModel
import com.nyxtra.vpn.ui.screens.logs.LogsScreen
import com.nyxtra.vpn.ui.screens.logs.LogsViewModel
import com.nyxtra.vpn.ui.screens.perapp.PerAppProxyScreen
import com.nyxtra.vpn.ui.screens.perapp.PerAppProxyViewModel
import com.nyxtra.vpn.ui.screens.profiles.ProfileEditScreen
import com.nyxtra.vpn.ui.screens.profiles.ProfilesScreen
import com.nyxtra.vpn.ui.screens.profiles.ProfilesViewModel
import com.nyxtra.vpn.ui.screens.settings.SettingsScreen
import com.nyxtra.vpn.ui.screens.settings.SettingsViewModel
import com.nyxtra.vpn.ui.theme.NyxtraBackground
import com.nyxtra.vpn.ui.theme.NyxtraCardBorder
import com.nyxtra.vpn.ui.theme.NyxtraNeonGreen
import com.nyxtra.vpn.ui.theme.NyxtraSurface
import com.nyxtra.vpn.ui.theme.TextMuted

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Shared ViewModels across screens
    val dashboardViewModel: DashboardViewModel = viewModel()
    val profilesViewModel: ProfilesViewModel = viewModel()
    val perAppViewModel: PerAppProxyViewModel = viewModel()
    val logsViewModel: LogsViewModel = viewModel()
    val settingsViewModel: SettingsViewModel = viewModel()

    val showBottomBar = currentRoute in Screen.bottomNavItems.map { it.route }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(NyxtraBackground),
        containerColor = NyxtraBackground,
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    modifier = Modifier
                        .navigationBarsPadding()
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                        .border(1.dp, NyxtraCardBorder, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                    containerColor = NyxtraSurface,
                    tonalElevation = 0.dp
                ) {
                    Screen.bottomNavItems.forEach { screen ->
                        val isSelected = currentRoute == screen.route
                        NavigationBarItem(
                            selected = isSelected,
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
                                screen.icon?.let { icon ->
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = screen.title,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            },
                            label = {
                                Text(
                                    text = screen.title,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = NyxtraNeonGreen,
                                selectedTextColor = NyxtraNeonGreen,
                                unselectedIconColor = TextMuted,
                                unselectedTextColor = TextMuted,
                                indicatorColor = NyxtraNeonGreen.copy(alpha = 0.12f)
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    viewModel = dashboardViewModel,
                    onNavigateToProfiles = { navController.navigate(Screen.Profiles.route) }
                )
            }

            composable(Screen.Profiles.route) {
                ProfilesScreen(
                    viewModel = profilesViewModel,
                    onNavigateToEdit = { navController.navigate(Screen.ProfileEdit.route) }
                )
            }

            composable(Screen.ProfileEdit.route) {
                ProfileEditScreen(
                    viewModel = profilesViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.PerApp.route) {
                PerAppProxyScreen(viewModel = perAppViewModel)
            }

            composable(Screen.Logs.route) {
                LogsScreen(viewModel = logsViewModel)
            }

            composable(Screen.Settings.route) {
                SettingsScreen(viewModel = settingsViewModel)
            }
        }
    }
}
