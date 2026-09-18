package com.zoti321.c2cmarket.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.zoti321.c2cmarket.R

private data class BottomNavItem(
    val route: String,
    val labelRes: Int,
    val icon: @Composable () -> Unit,
)

@Composable
fun BottomNavBar(navController: NavHostController) {
    val items = listOf(
        BottomNavItem(Routes.HOME, R.string.nav_home) {
            Icon(Icons.Default.Home, contentDescription = null)
        },
        BottomNavItem(Routes.CATEGORY, R.string.nav_category) {
            Icon(Icons.Default.Category, contentDescription = null)
        },
        BottomNavItem(Routes.CART, R.string.nav_cart) {
            Icon(Icons.Default.ShoppingCart, contentDescription = null)
        },
        BottomNavItem(Routes.PROFILE, R.string.nav_profile) {
            Icon(Icons.Default.Person, contentDescription = null)
        },
    )

    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry.value?.destination

    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(
                selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(Routes.HOME) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = item.icon,
                label = { Text(stringResource(item.labelRes)) },
            )
        }
    }
}
