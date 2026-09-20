package com.zoti321.c2cmarket.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.zoti321.c2cmarket.ui.address.AddressFormScreen
import com.zoti321.c2cmarket.ui.address.AddressListScreen
import com.zoti321.c2cmarket.ui.cart.CartScreen
import com.zoti321.c2cmarket.ui.category.CategoryListScreen
import com.zoti321.c2cmarket.ui.category.CategoryProductsScreen
import com.zoti321.c2cmarket.ui.checkout.CheckoutScreen
import com.zoti321.c2cmarket.ui.home.HomeScreen
import com.zoti321.c2cmarket.ui.listing.CreateListingScreen
import com.zoti321.c2cmarket.ui.order.OrderDetailScreen
import com.zoti321.c2cmarket.ui.product.ProductDetailScreen
import com.zoti321.c2cmarket.ui.profile.ProfileScreen
import com.zoti321.c2cmarket.ui.search.SearchScreen

@Composable
fun C2CApp(pendingOrderId: Long? = null) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute in Routes.bottomNavRoutes ||
        currentRoute?.startsWith("category/") == true

    LaunchedEffect(pendingOrderId) {
        pendingOrderId?.let { orderId ->
            navController.navigate(Routes.order(orderId)) {
                launchSingleTop = true
            }
        }
    }

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
                    onSearchClick = { navController.navigate(Routes.SEARCH) },
                )
            }

            composable(Routes.SEARCH) {
                SearchScreen(
                    onBack = { navController.popBackStack() },
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
                    onProductClick = { id ->
                        navController.navigate(Routes.product(id))
                    },
                    onCreateListing = { navController.navigate(Routes.CREATE_LISTING) },
                    onEditListing = { catalogId ->
                        navController.navigate(Routes.editListing(catalogId))
                    },
                    onManageAddresses = { navController.navigate(Routes.ADDRESS_LIST) },
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
                    onEditListing = { catalogId ->
                        navController.navigate(Routes.editListing(catalogId))
                    },
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
                    onAddAddress = { navController.navigate(Routes.ADDRESS_CREATE) },
                    onManageAddresses = { navController.navigate(Routes.ADDRESS_LIST) },
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

            composable(Routes.CREATE_LISTING) {
                CreateListingScreen(
                    onBack = { navController.popBackStack() },
                )
            }

            composable(
                route = Routes.EDIT_LISTING,
                arguments = listOf(
                    navArgument(Routes.LISTING_CATALOG_ID_ARG) { type = NavType.IntType },
                ),
            ) {
                CreateListingScreen(
                    onBack = { navController.popBackStack() },
                )
            }

            composable(Routes.ADDRESS_LIST) {
                AddressListScreen(
                    onBack = { navController.popBackStack() },
                    onCreateAddress = { navController.navigate(Routes.ADDRESS_CREATE) },
                    onEditAddress = { id ->
                        navController.navigate(Routes.editAddress(id))
                    },
                )
            }

            composable(Routes.ADDRESS_CREATE) {
                AddressFormScreen(
                    isEdit = false,
                    onBack = { navController.popBackStack() },
                )
            }

            composable(
                route = Routes.ADDRESS_EDIT,
                arguments = listOf(
                    navArgument(Routes.ADDRESS_ID_ARG) { type = NavType.LongType },
                ),
            ) {
                AddressFormScreen(
                    isEdit = true,
                    onBack = { navController.popBackStack() },
                )
            }
        }
    }
}
