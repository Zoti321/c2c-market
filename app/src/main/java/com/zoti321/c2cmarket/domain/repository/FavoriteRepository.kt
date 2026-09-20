package com.zoti321.c2cmarket.domain.repository

import com.zoti321.c2cmarket.domain.model.Product
import kotlinx.coroutines.flow.Flow

interface FavoriteRepository {
    fun isFavorite(productId: Int): Flow<Boolean>

    fun observeFavorites(): Flow<List<Product>>

    suspend fun toggleFavorite(product: Product): Result<Boolean>

    suspend fun removeFavorite(productId: Int): Result<Unit>
}
