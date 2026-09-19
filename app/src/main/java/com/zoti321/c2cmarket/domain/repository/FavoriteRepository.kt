package com.zoti321.c2cmarket.domain.repository

import com.zoti321.c2cmarket.domain.model.Product
import kotlinx.coroutines.flow.Flow

interface FavoriteRepository {
    fun isFavorite(productId: Int): Flow<Boolean>

    suspend fun toggleFavorite(product: Product): Result<Boolean>
}
