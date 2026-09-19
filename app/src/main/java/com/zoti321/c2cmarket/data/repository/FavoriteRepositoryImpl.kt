package com.zoti321.c2cmarket.data.repository

import com.zoti321.c2cmarket.data.local.dao.FavoriteDao
import com.zoti321.c2cmarket.data.local.entity.FavoriteEntity
import com.zoti321.c2cmarket.domain.model.Product
import com.zoti321.c2cmarket.domain.repository.FavoriteRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

@Singleton
class FavoriteRepositoryImpl @Inject constructor(
    private val favoriteDao: FavoriteDao,
) : FavoriteRepository {

    override fun isFavorite(productId: Int): Flow<Boolean> =
        favoriteDao.observeIsFavorite(productId)

    override suspend fun toggleFavorite(product: Product): Result<Boolean> = runCatching {
        val currentlyFavorite = favoriteDao.isFavorite(product.id)
        if (currentlyFavorite) {
            favoriteDao.deleteByProductId(product.id)
            false
        } else {
            favoriteDao.insert(FavoriteEntity(product.id, System.currentTimeMillis()))
            true
        }
    }
}
