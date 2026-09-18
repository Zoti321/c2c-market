package com.zoti321.c2cmarket.data.paging

import androidx.paging.PagingSource
import com.zoti321.c2cmarket.data.remote.FakeStoreApi
import com.zoti321.c2cmarket.data.remote.dto.ProductDto
import com.zoti321.c2cmarket.data.remote.dto.RatingDto
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FakeStorePagingSourceTest {

    private val fakeApi = object : FakeStoreApi {
        override suspend fun getProducts(limit: Int?, sort: String?): List<ProductDto> =
            (1..12).map { id ->
                ProductDto(
                    id = id,
                    title = "Product $id",
                    price = id.toDouble(),
                    description = "Desc",
                    category = "electronics",
                    image = "https://example.com/$id.png",
                    rating = RatingDto(4.0, 1),
                )
            }

        override suspend fun getProduct(id: Int): ProductDto = error("unused")

        override suspend fun getCategories(): List<String> = error("unused")

        override suspend fun getProductsByCategory(category: String): List<ProductDto> =
            error("unused")
    }

    @Test
    fun load_returnsFirstPageWithNextKey() = runTest {
        val source = FakeStorePagingSource(fakeApi, pageSize = 10)
        val result = source.load(PagingSource.LoadParams.Refresh(key = null, loadSize = 10, placeholdersEnabled = false))

        assertTrue(result is PagingSource.LoadResult.Page)
        val page = result as PagingSource.LoadResult.Page
        assertEquals(10, page.data.size)
        assertEquals(1, page.nextKey)
        assertNull(page.prevKey)
    }

    @Test
    fun load_returnsLastPageWithoutNextKey() = runTest {
        val source = FakeStorePagingSource(fakeApi, pageSize = 10)
        val result = source.load(PagingSource.LoadParams.Append(key = 1, loadSize = 10, placeholdersEnabled = false))

        assertTrue(result is PagingSource.LoadResult.Page)
        val page = result as PagingSource.LoadResult.Page
        assertEquals(2, page.data.size)
        assertNull(page.nextKey)
    }
}
