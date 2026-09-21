package com.zoti321.c2cmarket.ui.navigation

import android.net.Uri

object Routes {
    const val HOME = "home"
    const val CATEGORY = "category"
    const val CART = "cart"
    const val PROFILE = "profile"

    const val SEARCH = "search"
    const val FAVORITES = "favorites"

    const val PRODUCT = "product/{id}"
    const val PRODUCT_ID_ARG = "id"
    fun product(id: Int) = "product/$id"

    const val CHECKOUT = "checkout"

    const val ORDER = "order/{id}"
    const val ORDER_ID_ARG = "id"
    fun order(id: Long) = "order/$id"

    const val CREATE_LISTING = "listing/create"
    const val EDIT_LISTING = "listing/edit/{catalogId}"
    const val LISTING_CATALOG_ID_ARG = "catalogId"
    fun editListing(catalogId: Int) = "listing/edit/$catalogId"

    const val ADDRESS_LIST = "addresses"
    const val ADDRESS_CREATE = "addresses/create"
    const val ADDRESS_EDIT = "addresses/edit/{addressId}"
    const val ADDRESS_ID_ARG = "addressId"
    fun editAddress(addressId: Long) = "addresses/edit/$addressId"

    const val CONVERSATIONS = "conversations/{role}"
    const val CONVERSATION_ROLE_ARG = "role"
    const val CONVERSATIONS_BUYER = "conversations/buyer"
    const val CONVERSATIONS_SELLER = "conversations/seller"
    const val CHAT = "chat/{conversationId}"
    const val CONVERSATION_ID_ARG = "conversationId"
    fun chat(conversationId: Long) = "chat/$conversationId"

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
