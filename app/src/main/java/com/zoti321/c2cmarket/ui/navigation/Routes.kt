package com.zoti321.c2cmarket.ui.navigation

import android.net.Uri

object Routes {
    const val HOME = "home"
    const val CATEGORY = "category"
    const val CART = "cart"
    const val PROFILE = "profile"

    const val PRODUCT = "product/{id}"
    const val PRODUCT_ID_ARG = "id"
    fun product(id: Int) = "product/$id"

    const val CHECKOUT = "checkout"

    const val ORDER = "order/{id}"
    const val ORDER_ID_ARG = "id"
    fun order(id: Long) = "order/$id"

    object Category {
        const val LIST = "category/list"
        const val PRODUCTS = "category/products/{categorySlug}"
        const val CATEGORY_SLUG_ARG = "categorySlug"

        fun products(categorySlug: String): String =
            "category/products/${Uri.encode(categorySlug)}"
    }

    const val CATEGORY_SLUG_ARG = Category.CATEGORY_SLUG_ARG

    val bottomNavRoutes = setOf(HOME, CATEGORY, CART, PROFILE)
}
