package com.zoti321.c2cmarket.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.zoti321.c2cmarket.domain.error.ProductNotFoundException
import com.zoti321.c2cmarket.data.mapper.toDomain
import com.zoti321.c2cmarket.data.paging.FakeStorePagingSource
import com.zoti321.c2cmarket.data.remote.FakeStoreApi
import com.zoti321.c2cmarket.domain.model.Product
import com.zoti321.c2cmarket.domain.repository.ProductRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.SerializationException

@Singleton
class FakeStoreProductRepository @Inject constructor(
    private val api: FakeStoreApi,
) : ProductRepository {

    override fun pagingProducts(pageSize: Int, sort: String): Flow<PagingData<Product>> =
        Pager(
            config = PagingConfig(
                pageSize = pageSize,
                prefetchDistance = pageSize,
                enablePlaceholders = false,
            ),
            pagingSourceFactory = { FakeStorePagingSource(api, sort, pageSize) },
        ).flow

    override suspend fun getProduct(id: Int): Result<Product> = runCatching {
        try {
            val dto = api.getProduct(id)
            if (dto.id <= 0 || dto.title.isBlank()) {
                throw ProductNotFoundException(id)
            }
            dto.toDomain()
        } catch (_: SerializationException) {
            throw ProductNotFoundException(id)
        }
    }

    override suspend fun getCategories(): Result<List<String>> = runCatching {
        api.getCategories()
    }

    override suspend fun getProductsByCategory(category: String): Result<List<Product>> =
        runCatching {
            api.getProductsByCategory(category).map { it.toDomain() }
        }
}
