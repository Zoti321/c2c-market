package com.zoti321.c2cmarket.domain.repository

import com.zoti321.c2cmarket.domain.model.ListingInput
import com.zoti321.c2cmarket.domain.model.ListingStatus
import com.zoti321.c2cmarket.domain.model.Product
import kotlinx.coroutines.flow.Flow

interface ListingRepository {
    fun observeAsProducts(): Flow<List<Product>>

    fun observeByCategory(category: String): Flow<List<Product>>

    fun observeMyListings(): Flow<List<Product>>

    suspend fun getProductByCatalogId(catalogId: Int): Result<Product>

    suspend fun getSellerId(catalogId: Int): String?

    suspend fun create(input: ListingInput): Result<Product>

    suspend fun update(catalogId: Int, input: ListingInput): Result<Product>

    suspend fun delete(catalogId: Int): Result<Unit>

    suspend fun updateStatus(catalogId: Int, status: ListingStatus): Result<Unit>

    suspend fun markReservedForCheckout(catalogIds: List<Int>)

    suspend fun markSoldForOrder(orderId: Long)

    suspend fun markAvailableForOrder(orderId: Long)

    suspend fun searchLocal(query: String): List<Product>
}
