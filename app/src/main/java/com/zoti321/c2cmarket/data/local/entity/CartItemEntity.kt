package com.zoti321.c2cmarket.data.local.entity

import androidx.room.Entity

@Entity(
    tableName = "cart_items",
    primaryKeys = ["userId", "productId"],
)
data class CartItemEntity(
    val userId: String,
    val productId: Int,
    val title: String,
    val unitPrice: Double,
    val imageUrl: String,
    val quantity: Int,
    val addedAt: Long,
)
