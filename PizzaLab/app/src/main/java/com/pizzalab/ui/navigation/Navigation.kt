package com.pizzalab.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.pizzalab.ui.calculator.CalculatorScreen
import com.pizzalab.ui.cooking.CookingScreen
import com.pizzalab.ui.help.HelpScreen
import com.pizzalab.ui.process.ProcessScreen

/**
 * Bottom navigation destinations for PizzaLab.
 */
sealed class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    data object Calculator : BottomNavItem(
        route = "calculator",
        label = "Calcolatore",
        icon = Icons.Filled.Calculate
    )

    data object Process : BottomNavItem(
        route = "process",
        label = "Processo",
        icon = Icons.Filled.Timer
    )

    data object Cooking : BottomNavItem(
        route = "cooking",
        label = "Cottura",
        icon = Icons.Filled.LocalFireDepartment
    )

    data object Help : BottomNavItem(
        route = "help",
        label = "Aiuto",
        icon = Icons.Filled.HelpOutline
    )
}

val bottomNavItems = listOf(
    BottomNavItem.Calculator,
    BottomNavItem.Process,
    BottomNavItem.Cooking,
    BottomNavItem.Help
)

/**
 * Bottom navigation bar with five tabs.
 */
@Composable
fun PizzaLabBottomBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    NavigationBar {
        bottomNavItems.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                onClick = {
                    navController.navigate(item.route) {
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

/**
 * Main navigation host for PizzaLab screens.
 */
@Composable
fun PizzaLabNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = BottomNavItem.Calculator.route,
        modifier = modifier
    ) {
        composable(BottomNavItem.Calculator.route) {
            CalculatorScreen(
                onStartProcess = {
                    navController.navigate(BottomNavItem.Process.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
        composable(BottomNavItem.Process.route) {
            ProcessScreen()
        }
        composable(BottomNavItem.Cooking.route) {
            CookingScreen()
        }
        composable(BottomNavItem.Help.route) {
            HelpScreen()
        }
    }
}
