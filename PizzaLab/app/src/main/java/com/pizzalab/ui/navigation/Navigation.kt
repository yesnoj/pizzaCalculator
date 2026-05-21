package com.pizzalab.ui.navigation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.pizzalab.ui.theme.QuadernoColors

sealed class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    data object Calculator : BottomNavItem(route = "calculator", label = "Calcolo", icon = Icons.Filled.Calculate)
    data object Process : BottomNavItem(route = "process", label = "Processo", icon = Icons.Filled.Timer)
    data object Cooking : BottomNavItem(route = "cooking", label = "Cottura", icon = Icons.Filled.LocalFireDepartment)
    data object Help : BottomNavItem(route = "help", label = "Glossario", icon = Icons.Filled.HelpOutline)
}

val bottomNavItems = listOf(BottomNavItem.Calculator, BottomNavItem.Process, BottomNavItem.Cooking, BottomNavItem.Help)

@Composable
fun PizzaLabBottomBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val dashedColor = QuadernoColors.RuleDots

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(QuadernoColors.Paper)
            .drawBehind {
                // dashed top border
                drawLine(
                    color = dashedColor,
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(4.dp.toPx(), 4.dp.toPx()))
                )
            }
            .padding(horizontal = 4.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        bottomNavItems.forEach { item ->
            val isSelected = currentDestination?.hierarchy?.any { it.route == item.route } == true

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ) {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                    modifier = Modifier.padding(vertical = 6.dp),
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        modifier = Modifier.size(20.dp),
                        tint = if (isSelected) QuadernoColors.Primary else QuadernoColors.Ink3,
                    )
                    Text(
                        text = item.label,
                        style = TextStyle(
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontStyle = if (isSelected) FontStyle.Normal else FontStyle.Italic,
                            color = if (isSelected) QuadernoColors.Primary else QuadernoColors.Ink3,
                        ),
                    )
                    // Active indicator — 24x2dp underline
                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .width(24.dp)
                                .height(2.dp)
                                .background(QuadernoColors.Primary, RoundedCornerShape(1.dp)),
                        )
                    } else {
                        Spacer(modifier = Modifier.height(2.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun PizzaLabNavHost(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(navController = navController, startDestination = BottomNavItem.Calculator.route, modifier = modifier) {
        composable(BottomNavItem.Calculator.route) {
            CalculatorScreen(onStartProcess = {
                navController.navigate(BottomNavItem.Process.route) {
                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            })
        }
        composable(BottomNavItem.Process.route) {
            ProcessScreen(onNavigateToCooking = {
                navController.navigate(BottomNavItem.Cooking.route) {
                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            })
        }
        composable(BottomNavItem.Cooking.route) { CookingScreen() }
        composable(BottomNavItem.Help.route) { HelpScreen() }
    }
}
