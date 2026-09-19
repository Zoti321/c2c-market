package com.zoti321.c2cmarket.domain.repository

import androidx.paging.PagingData
import com.zoti321.c2cmarket.domain.model.Product
import kotlinx.coroutines.flow.Flow

interface ProductRepository {
    fun pagingProducts(pageSize: Int = 10, sort: String = "desc"): Flow<PagingData<Product>>

    suspend fun getProduct(id: Int): Result<Product>

    suspend fun getCategories(): Result<List<String>>

    suspend fun getProductsByCategory(category: String): Result<List<Product>>
}
