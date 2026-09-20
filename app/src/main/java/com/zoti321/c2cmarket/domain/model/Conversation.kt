package com.zoti321.c2cmarket.domain.model

data class Conversation(
    val id: Long,
    val productId: Int,
    val productTitle: String,
    val productImageUrl: String,
    val sellerId: String,
    val sellerDisplayName: String,
    val buyerId: String,
    val lastMessagePreview: String,
    val lastMessageAt: Long,
    val unreadCount: Int,
    val createdAt: Long,
)
