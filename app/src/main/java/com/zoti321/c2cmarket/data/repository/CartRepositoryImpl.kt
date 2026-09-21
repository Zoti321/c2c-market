package com.zoti321.c2cmarket.data.repository

import com.zoti321.c2cmarket.data.local.dao.CartDao
import com.zoti321.c2cmarket.data.mapper.toCartItemEntity
import com.zoti321.c2cmarket.data.mapper.toDomain
import com.zoti321.c2cmarket.domain.model.CartItem
import com.zoti321.c2cmarket.domain.model.Product
import com.zoti321.c2cmarket.domain.repository.AuthRepository
import com.zoti321.c2cmarket.domain.repository.CartRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

@Singleton
class CartRepositoryImpl @Inject constructor(
    private val cartDao: CartDao,
    private val authRepository: AuthRepository,
) : CartRepository {

    override fun observeItems(): Flow<List<CartItem>> =
        authRepository.currentUserId().flatMapLatest { userId ->
            cartDao.observeAll(userId).map { list -> list.map { it.toDomain() } }
        }

    override suspend fun addItem(product: Product): Result<Unit> = runCatching {
        val userId = authRepository.currentUserId().first()
        val existing = cartDao.getByProductId(userId, product.id)
        val now = System.currentTimeMillis()
        if (existing == null) {
            cartDao.insert(product.toCartItemEntity(userId, quantity = 1, addedAt = now))
        } else {
            cartDao.updateItem(
                userId = userId,
                id = product.id,
                title = product.title,
                price = product.price,
                imageUrl = product.imageUrl,
                qty = existing.quantity + 1,
            )
        }
    }

    override suspend fun updateQuantity(productId: Int, quantity: Int): Result<Unit> =
        runCatching {
            require(quantity >= 1) { "Quantity must be >= 1" }
            val userId = authRepository.currentUserId().first()
            cartDao.updateQuantity(userId, productId, quantity)
        }

    override suspend fun removeItem(productId: Int): Result<Unit> = runCatching {
        val userId = authRepository.currentUserId().first()
        cartDao.deleteByProductId(userId, productId)
    }

    override suspend fun clearAll(): Result<Unit> = runCatching {
        val userId = authRepository.currentUserId().first()
        cartDao.clearForUser(userId)
    }
}
