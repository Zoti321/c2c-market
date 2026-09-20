package com.zoti321.c2cmarket.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val guestId: String,
    val totalAmount: Double,
    val status: String,
    val createdAt: Long,
    val shippingReceiverName: String? = null,
    val shippingPhone: String? = null,
    val shippingAddress: String? = null,
    val meetupLocation: String? = null,
    val buyerMeetupConfirmed: Boolean = false,
    val sellerMeetupConfirmed: Boolean = false,
)
