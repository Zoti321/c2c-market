package com.zoti321.c2cmarket.domain.repository

import com.zoti321.c2cmarket.domain.model.CartItem
import com.zoti321.c2cmarket.domain.model.Product
import kotlinx.coroutines.flow.Flow

interface CartRepository {
    fun observeItems(): Flow<List<CartItem>>

    suspend fun addItem(product: Product): Result<Unit>

    suspend fun updateQuantity(productId: Int, quantity: Int): Result<Unit>

    suspend fun removeItem(productId: Int): Result<Unit>

    suspend fun clearAll(): Result<Unit>
}
