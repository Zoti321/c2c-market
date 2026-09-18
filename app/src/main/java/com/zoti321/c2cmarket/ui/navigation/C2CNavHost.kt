package com.zoti321.c2cmarket.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.zoti321.c2cmarket.ui.cart.CartScreen
import com.zoti321.c2cmarket.ui.category.CategoryScreen
import com.zoti321.c2cmarket.ui.home.HomeScreen
import com.zoti321.c2cmarket.ui.profile.ProfileScreen

@Composable
fun C2CApp() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = { BottomNavBar(navController) },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(Routes.HOME) { HomeScreen() }
            composable(Routes.CATEGORY) { CategoryScreen() }
            composable(Routes.CART) { CartScreen() }
            composable(Routes.PROFILE) { ProfileScreen() }
        }
    }
}
