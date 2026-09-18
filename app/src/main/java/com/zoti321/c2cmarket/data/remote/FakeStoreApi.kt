package com.zoti321.c2cmarket.data.remote

import com.zoti321.c2cmarket.data.remote.dto.ProductDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface FakeStoreApi {
    @GET("products")
    suspend fun getProducts(
        @Query("limit") limit: Int? = null,
        @Query("sort") sort: String? = null,
    ): List<ProductDto>

    @GET("products/{id}")
    suspend fun getProduct(@Path("id") id: Int): ProductDto

    @GET("products/categories")
    suspend fun getCategories(): List<String>

    @GET("products/category/{category}")
    suspend fun getProductsByCategory(
        @Path("category") category: String,
    ): List<ProductDto>
}
