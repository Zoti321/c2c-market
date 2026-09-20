package com.zoti321.c2cmarket.data.repository

import androidx.room.withTransaction
import com.zoti321.c2cmarket.data.error.InvalidListingException
import com.zoti321.c2cmarket.data.local.C2CDatabase
import com.zoti321.c2cmarket.data.local.dao.CartDao
import com.zoti321.c2cmarket.data.local.dao.FavoriteDao
import com.zoti321.c2cmarket.data.local.dao.ListingDao
import com.zoti321.c2cmarket.data.mapper.toEntity
import com.zoti321.c2cmarket.data.mapper.toProduct
import com.zoti321.c2cmarket.domain.error.ProductNotFoundException
import com.zoti321.c2cmarket.domain.model.ListingInput
import com.zoti321.c2cmarket.domain.model.Product
import com.zoti321.c2cmarket.domain.repository.AuthRepository
import com.zoti321.c2cmarket.domain.repository.ListingRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

@Singleton
class ListingRepositoryImpl @Inject constructor(
    private val database: C2CDatabase,
    private val listingDao: ListingDao,
    private val cartDao: CartDao,
    private val favoriteDao: FavoriteDao,
    private val authRepository: AuthRepository,
) : ListingRepository {

    override fun observeAsProducts(): Flow<List<Product>> =
        listingDao.observeAll().map { listings -> listings.map { it.toProduct() } }

    override fun observeByCategory(category: String): Flow<List<Product>> =
        listingDao.observeByCategory(category).map { listings -> listings.map { it.toProduct() } }

    override fun observeMyListings(): Flow<List<Product>> =
        authRepository.currentUserId().flatMapLatest { sellerId ->
            listingDao.observeBySellerId(sellerId).map { listings ->
                listings.map { it.toProduct() }
            }
        }

    override suspend fun getProductByCatalogId(catalogId: Int): Result<Product> = runCatching {
        val entity = listingDao.getByCatalogId(catalogId)
            ?: throw ProductNotFoundException(catalogId)
        entity.toProduct()
    }

    override suspend fun getSellerId(catalogId: Int): String? =
        listingDao.getByCatalogId(catalogId)?.sellerId

    override suspend fun create(input: ListingInput): Result<Product> = runCatching {
        validate(input)
        val now = System.currentTimeMillis()
        val catalogId = nextCatalogId()
        val sellerId = authRepository.currentUserId().first()
        val entity = input.toEntity(catalogId, now, sellerId)
        listingDao.insert(entity)
        entity.toProduct()
    }

    override suspend fun update(catalogId: Int, input: ListingInput): Result<Product> = runCatching {
        validate(input)
        val existing = listingDao.getByCatalogId(catalogId)
            ?: throw ProductNotFoundException(catalogId)
        val now = System.currentTimeMillis()
        val updated = existing.copy(
            title = input.title.trim(),
            price = input.price,
            description = input.description.trim(),
            category = input.category,
            imageUri = input.imageUri,
            updatedAt = now,
        )
        listingDao.update(updated)
        updated.toProduct()
    }

    override suspend fun delete(catalogId: Int): Result<Unit> = runCatching {
        database.withTransaction {
            listingDao.deleteByCatalogId(catalogId)
            cartDao.deleteByProductId(catalogId)
            favoriteDao.deleteByProductId(catalogId)
        }
    }

    override suspend fun searchLocal(query: String): List<Product> {
        val normalized = query.trim().lowercase()
        if (normalized.isEmpty()) return emptyList()
        return listingDao.observeAll().first()
            .map { it.toProduct() }
            .filter { it.title.lowercase().contains(normalized) }
    }

    private suspend fun nextCatalogId(): Int {
        val minId = listingDao.minCatalogId()
        return if (minId == null) -1 else minId - 1
    }

    private fun validate(input: ListingInput) {
        val message = when {
            input.title.isBlank() || input.title.length > 100 -> "标题无效"
            input.price <= 0 -> "价格必须大于 0"
            input.description.isBlank() || input.description.length > 500 -> "描述无效"
            input.imageUri.isBlank() -> "请选择图片"
            else -> null
        }
        if (message != null) throw InvalidListingException(message)
    }
}
