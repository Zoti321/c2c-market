package com.zoti321.c2cmarket.data.mapper

import com.zoti321.c2cmarket.data.local.entity.CartItemEntity
import com.zoti321.c2cmarket.domain.model.CartItem
import com.zoti321.c2cmarket.domain.model.Product
import java.time.Instant

fun CartItemEntity.toDomain(): CartItem = CartItem(
    productId = productId,
    title = title,
    unitPrice = unitPrice,
    imageUrl = imageUrl,
    quantity = quantity,
    addedAt = Instant.ofEpochMilli(addedAt),
)

fun Product.toCartItemEntity(userId: String, quantity: Int, addedAt: Long): CartItemEntity = CartItemEntity(
    userId = userId,
    productId = id,
    title = title,
    unitPrice = price,
    imageUrl = imageUrl,
    quantity = quantity,
    addedAt = addedAt,
)
