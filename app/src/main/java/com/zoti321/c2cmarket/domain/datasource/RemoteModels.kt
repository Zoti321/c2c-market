package com.zoti321.c2cmarket.domain.datasource

import com.zoti321.c2cmarket.domain.model.ListingStatus
import com.zoti321.c2cmarket.domain.model.OrderStatus

enum class SyncState {
    SYNCED,
    PENDING,
    FAILED,
}

data class RemoteConversation(
    val remoteId: String,
    val buyerId: String,
    val sellerId: String,
    val productId: Int,
    val productTitle: String,
    val productImageUrl: String,
    val buyerDisplayName: String,
    val sellerDisplayName: String,
    val lastMessagePreview: String,
    val lastMessageAt: Long,
    val unreadCount: Int,
    val sellerUnreadCount: Int,
    val createdAt: Long,
)

data class RemoteMessage(
    val remoteId: String,
    val senderId: String,
    val body: String,
    val sentAt: Long,
    val isRead: Boolean,
)

data class RemoteOrder(
    val remoteId: String,
    val buyerId: String,
    val sellerId: String,
    val status: OrderStatus,
    val lineItems: List<RemoteOrderLineItem>,
    val totalAmount: Double,
    val meetupLocation: String?,
    val buyerMeetupConfirmed: Boolean,
    val sellerMeetupConfirmed: Boolean,
    val createdAt: Long,
    val updatedAt: Long,
)

data class RemoteOrderLineItem(
    val productId: Int,
    val title: String,
    val unitPrice: Double,
    val quantity: Int,
    val imageUrl: String,
)

data class OrderSyncExtras(
    val buyerMeetupConfirmed: Boolean = false,
    val sellerMeetupConfirmed: Boolean = false,
    val meetupLocation: String? = null,
)

data class RemoteListing(
    val catalogId: Int,
    val sellerId: String,
    val title: String,
    val price: Double,
    val description: String,
    val category: String,
    val imageUrl: String,
    val meetupLocation: String?,
    val status: ListingStatus,
    val createdAt: Long,
    val updatedAt: Long,
)
