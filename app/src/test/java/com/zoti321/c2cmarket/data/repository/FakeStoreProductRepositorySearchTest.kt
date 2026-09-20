package com.zoti321.c2cmarket.data.repository

import com.zoti321.c2cmarket.data.remote.FakeStoreApi
import com.zoti321.c2cmarket.domain.model.ProductSource
import com.zoti321.c2cmarket.domain.model.Rating
import com.zoti321.c2cmarket.domain.model.SearchResult
import com.zoti321.c2cmarket.domain.model.Product
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FakeStoreProductRepositorySearchTest {

    @Test
    fun searchProducts_mergesLocalListingsFirst() = runTest {
        val localListing = Product(
            id = -1,
            title = "Local Phone",
            price = 10.0,
            description = "Local",
            category = "electronics",
            imageUrl = "content://local",
            rating = Rating(0.0, 0),
            source = ProductSource.LOCAL_LISTING,
        )
        val api = object : FakeStoreApi {
            override suspend fun getProducts(limit: Int?, sort: String?) = listOf(
                com.zoti321.c2cmarket.data.remote.dto.ProductDto(
                    id = 1,
                    title = "Remote Phone",
                    price = 20.0,
                    description = "Remote",
                    category = "electronics",
                    image = "https://example.com/r.png",
                    rating = com.zoti321.c2cmarket.data.remote.dto.RatingDto(rate = 4.0, count = 1),
                ),
            )

            override suspend fun getProduct(id: Int) = error("unused")

            override suspend fun getCategories() = error("unused")

            override suspend fun getProductsByCategory(category: String) = error("unused")
        }
        val repository = FakeStoreProductRepository(
            api,
            FakeListingRepository(localProducts = listOf(localListing)),
        )

        val result = repository.searchProducts("phone").first { it is SearchResult.Success }
        assertTrue(result is SearchResult.Success)
        val products = (result as SearchResult.Success).products
        assertEquals(-1, products.first().id)
        assertEquals(2, products.size)
    }
}
