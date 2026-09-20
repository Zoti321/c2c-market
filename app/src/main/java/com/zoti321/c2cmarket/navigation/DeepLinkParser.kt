package com.zoti321.c2cmarket.navigation

import android.net.Uri

sealed interface DeepLinkDestination {
    data class Product(val productId: Int) : DeepLinkDestination

    data class Order(val orderId: Long) : DeepLinkDestination

    data class Chat(val conversationId: Long) : DeepLinkDestination
}

object DeepLinkParser {
    private const val SCHEME = "c2cmarket"
    private const val HOST = "app"

    fun parse(uri: Uri): DeepLinkDestination? {
        if (uri.scheme != SCHEME || uri.host != HOST) {
            return null
        }
        val segments = uri.pathSegments
        return if (segments.size != 2) {
            null
        } else {
            when (segments[0]) {
                "product" -> segments[1].toIntOrNull()?.let { DeepLinkDestination.Product(it) }
                "order" -> segments[1].toLongOrNull()?.let { DeepLinkDestination.Order(it) }
                "chat" -> segments[1].toLongOrNull()?.let { DeepLinkDestination.Chat(it) }
                else -> null
            }
        }
    }

    fun productUri(productId: Int): Uri = Uri.parse("$SCHEME://$HOST/product/$productId")

    fun orderUri(orderId: Long): Uri = Uri.parse("$SCHEME://$HOST/order/$orderId")

    fun chatUri(conversationId: Long): Uri = Uri.parse("$SCHEME://$HOST/chat/$conversationId")

    fun shareText(title: String, productId: Int): String = "$title\n${productUri(productId)}"
}
