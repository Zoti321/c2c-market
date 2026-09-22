package com.zoti321.c2cmarket.domain.model

data class Conversation(
    val id: Long,
    val productId: Int,
    val productTitle: String,
    val productImageUrl: String,
    val sellerId: String,
    val sellerDisplayName: String,
    val buyerId: String,
    val buyerDisplayName: String,
    val lastMessagePreview: String,
    val lastMessageAt: Long,
    /** 买家视角未读数 */
    val unreadCount: Int,
    /** 卖家视角未读数 */
    val sellerUnreadCount: Int,
    val createdAt: Long,
    val remoteId: String? = null,
)
