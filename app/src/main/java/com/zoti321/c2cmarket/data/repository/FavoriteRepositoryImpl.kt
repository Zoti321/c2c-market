package com.zoti321.c2cmarket.data.repository

import com.zoti321.c2cmarket.data.local.dao.FavoriteDao
import com.zoti321.c2cmarket.data.local.entity.FavoriteEntity
import com.zoti321.c2cmarket.domain.model.Product
import com.zoti321.c2cmarket.domain.repository.AuthRepository
import com.zoti321.c2cmarket.domain.repository.FavoriteRepository
import com.zoti321.c2cmarket.domain.repository.ProductRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

@Singleton
class FavoriteRepositoryImpl @Inject constructor(
    private val favoriteDao: FavoriteDao,
    private val authRepository: AuthRepository,
    private val productRepository: ProductRepository,
) : FavoriteRepository {

    override fun isFavorite(productId: Int): Flow<Boolean> =
        authRepository.currentUserId().flatMapLatest { userId ->
            favoriteDao.observeIsFavorite(userId, productId)
        }

    override fun observeFavorites(): Flow<List<Product>> =
        authRepository.currentUserId().flatMapLatest { userId ->
            favoriteDao.observeAll(userId).map { favorites ->
                favorites.mapNotNull { favorite ->
                    productRepository.getProduct(favorite.productId).getOrNull()
                }
            }
        }

    override suspend fun toggleFavorite(product: Product): Result<Boolean> = runCatching {
        val userId = authRepository.currentUserId().first()
        val currentlyFavorite = favoriteDao.isFavorite(userId, product.id)
        if (currentlyFavorite) {
            favoriteDao.deleteByProductId(userId, product.id)
            false
        } else {
            favoriteDao.insert(FavoriteEntity(userId, product.id, System.currentTimeMillis()))
            true
        }
    }

    override suspend fun removeFavorite(productId: Int): Result<Unit> = runCatching {
        val userId = authRepository.currentUserId().first()
        favoriteDao.deleteByProductId(userId, productId)
    }
}
