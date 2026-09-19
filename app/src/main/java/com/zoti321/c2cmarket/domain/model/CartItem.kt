package com.zoti321.c2cmarket.domain.model

import java.time.Instant

data class CartItem(
    val productId: Int,
    val title: String,
    val unitPrice: Double,
    val imageUrl: String,
    val quantity: Int,
    val addedAt: Instant,
)
