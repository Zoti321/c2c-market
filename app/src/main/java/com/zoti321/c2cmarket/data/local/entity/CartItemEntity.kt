package com.zoti321.c2cmarket.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cart_items")
data class CartItemEntity(
    @PrimaryKey val productId: Int,
    val title: String,
    val unitPrice: Double,
    val imageUrl: String,
    val quantity: Int,
    val addedAt: Long,
)
