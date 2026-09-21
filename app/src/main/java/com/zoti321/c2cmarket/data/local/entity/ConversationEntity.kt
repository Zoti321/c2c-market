package com.zoti321.c2cmarket.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "conversations",
    indices = [
        Index(
            value = ["buyerId", "sellerId", "productId"],
            unique = true,
        ),
    ],
)
data class ConversationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val productId: Int,
    val productTitle: String,
    val productImageUrl: String,
    val sellerId: String,
    val sellerDisplayName: String,
    val buyerId: String,
    val buyerDisplayName: String = "游客",
    val lastMessagePreview: String,
    val lastMessageAt: Long,
    val unreadCount: Int,
    val sellerUnreadCount: Int = 0,
    val createdAt: Long,
    val remoteId: String? = null,
)
