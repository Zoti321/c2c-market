package com.zoti321.c2cmarket.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.zoti321.c2cmarket.ui.cart.CartScreen
import com.zoti321.c2cmarket.ui.category.CategoryListScreen
import com.zoti321.c2cmarket.ui.category.CategoryProductsScreen
import com.zoti321.c2cmarket.ui.checkout.CheckoutScreen
import com.zoti321.c2cmarket.ui.home.HomeScreen
import com.zoti321.c2cmarket.ui.order.OrderDetailScreen
import com.zoti321.c2cmarket.ui.product.ProductDetailScreen
import com.zoti321.c2cmarket.ui.profile.ProfileScreen

@Composable
fun C2CApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute in Routes.bottomNavRoutes ||
        currentRoute?.startsWith("category/") == true

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomNavBar(navController)
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(Routes.HOME) {
                HomeScreen(
                    onProductClick = { id ->
                        navController.navigate(Routes.product(id))
                    },
                )
            }

            navigation(
                route = Routes.CATEGORY,
                startDestination = Routes.Category.LIST,
            ) {
                composable(Routes.Category.LIST) {
                    CategoryListScreen(
                        onCategoryClick = { slug ->
                            navController.navigate(Routes.Category.products(slug))
                        },
                    )
                }
                composable(
                    route = Routes.Category.PRODUCTS,
                    arguments = listOf(
                        navArgument(Routes.CATEGORY_SLUG_ARG) { type = NavType.StringType },
                    ),
                ) {
                    CategoryProductsScreen(
                        onBack = { navController.popBackStack() },
                        onProductClick = { id ->
                            navController.navigate(Routes.product(id))
                        },
                    )
                }
            }

            composable(Routes.CART) {
                CartScreen(
                    onCheckout = { navController.navigate(Routes.CHECKOUT) },
                    onGoHome = {
                        navController.navigate(Routes.HOME) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                )
            }

            composable(Routes.PROFILE) {
                ProfileScreen(
                    onOrderClick = { orderId ->
                        navController.navigate(Routes.order(orderId))
                    },
                )
            }

            composable(
                route = Routes.PRODUCT,
                arguments = listOf(
                    navArgument(Routes.PRODUCT_ID_ARG) { type = NavType.IntType },
                ),
            ) {
                ProductDetailScreen(
                    onBack = { navController.popBackStack() },
                )
            }

            composable(Routes.CHECKOUT) {
                CheckoutScreen(
                    onBack = { navController.popBackStack() },
                    onOrderPlaced = { orderId ->
                        navController.navigate(Routes.order(orderId)) {
                            popUpTo(Routes.CHECKOUT) { inclusive = true }
                        }
                    },
                )
            }

            composable(
                route = Routes.ORDER,
                arguments = listOf(
                    navArgument(Routes.ORDER_ID_ARG) { type = NavType.LongType },
                ),
            ) {
                OrderDetailScreen(
                    onBack = { navController.popBackStack() },
                )
            }
        }
    }
}
