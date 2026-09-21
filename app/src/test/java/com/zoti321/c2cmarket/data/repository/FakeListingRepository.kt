package com.zoti321.c2cmarket.data.repository

import com.zoti321.c2cmarket.domain.model.ListingInput
import com.zoti321.c2cmarket.domain.model.ListingStatus
import com.zoti321.c2cmarket.domain.model.Product
import com.zoti321.c2cmarket.domain.repository.ListingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeListingRepository(
    private val localProducts: List<Product> = emptyList(),
) : ListingRepository {

    override fun observeAsProducts(): Flow<List<Product>> = flowOf(localProducts)

    override fun observeByCategory(category: String): Flow<List<Product>> =
        flowOf(localProducts.filter { it.category == category })

    override fun observeMyListings(): Flow<List<Product>> = observeAsProducts()

    override suspend fun getProductByCatalogId(catalogId: Int): Result<Product> =
        localProducts.firstOrNull { it.id == catalogId }
            ?.let { Result.success(it) }
            ?: Result.failure(IllegalStateException("not found"))

    override suspend fun getSellerId(catalogId: Int): String? = "guest"

    override suspend fun create(input: ListingInput): Result<Product> =
        Result.failure(UnsupportedOperationException())

    override suspend fun update(catalogId: Int, input: ListingInput): Result<Product> =
        Result.failure(UnsupportedOperationException())

    override suspend fun delete(catalogId: Int): Result<Unit> =
        Result.failure(UnsupportedOperationException())

    override suspend fun updateStatus(catalogId: Int, status: ListingStatus): Result<Unit> =
        Result.success(Unit)

    override suspend fun markReservedForCheckout(catalogIds: List<Int>) = Unit

    override suspend fun markSoldForOrder(orderId: Long) = Unit

    override suspend fun markAvailableForOrder(orderId: Long) = Unit

    override suspend fun searchLocal(query: String): List<Product> {
        val normalized = query.trim().lowercase()
        return localProducts.filter { it.title.lowercase().contains(normalized) }
    }
}
