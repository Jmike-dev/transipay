package com.example.transipay.screen.home

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.transipay.navigation.HomeTab
import com.example.transipay.navigation.TransiBottomBar
import com.example.transipay.screen.fleet.FleetScreen
import com.example.transipay.screen.payments.PaymentsScreen
import com.example.transipay.screen.staff.StaffListScreen
import com.example.transipay.screen.terminal.TerminalScreen

@Composable
fun HomeScreen(onAddStaff: () -> Unit, onSignOut: () -> Unit) {
    // Separate controller for the tabs, so the bottom bar only lives on this screen
    val tabsNav = rememberNavController()
    val backStack by tabsNav.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            TransiBottomBar(
                currentRoute = currentRoute,
                onSelect = { tab ->
                    tabsNav.navigate(tab.route) {
                        popUpTo(tabsNav.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
            )
        },
    ) { padding ->
        NavHost(
            navController = tabsNav,
            startDestination = HomeTab.Fleet.route,
            modifier = Modifier.padding(padding),
        ) {
            composable(HomeTab.Fleet.route) { FleetScreen(onSignOut = onSignOut) }
            composable(HomeTab.Staff.route) { StaffListScreen(onAddStaff = onAddStaff) }
            composable(HomeTab.Payments.route) { PaymentsScreen() }
            composable(HomeTab.Terminal.route) { TerminalScreen() }
        }
    }
}