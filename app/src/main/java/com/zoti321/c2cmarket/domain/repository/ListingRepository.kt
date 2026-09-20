package com.zoti321.c2cmarket.domain.repository

import com.zoti321.c2cmarket.domain.model.ListingInput
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

    suspend fun searchLocal(query: String): List<Product>
}
