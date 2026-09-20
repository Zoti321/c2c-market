package com.zoti321.c2cmarket.ui.home

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.paging.PagingData
import com.zoti321.c2cmarket.domain.model.Product
import com.zoti321.c2cmarket.domain.model.Rating
import com.zoti321.c2cmarket.domain.model.SearchResult
import com.zoti321.c2cmarket.domain.repository.ProductRepository
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class HomeScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createComposeRule()

    @BindValue
    @JvmField
    val productRepository: ProductRepository = object : ProductRepository {
        private val products = listOf(
            Product(
                id = 1,
                title = "Test Product",
                price = 9.99,
                description = "Desc",
                category = "electronics",
                imageUrl = "https://example.com/img.png",
                rating = Rating(4.5, 10),
            ),
        )

        override fun pagingProducts(pageSize: Int, sort: String): Flow<PagingData<Product>> =
            flowOf(PagingData.from(products))

        override suspend fun getProduct(id: Int): Result<Product> = Result.success(products.first())

        override suspend fun getCategories(): Result<List<String>> =
            Result.success(listOf("electronics"))

        override suspend fun getProductsByCategory(category: String): Result<List<Product>> =
            Result.success(products)

        override suspend fun getAllProducts(): Result<List<Product>> = Result.success(products)

        override fun searchProducts(query: String): Flow<SearchResult> =
            flowOf(SearchResult.Success(products))
    }

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @Test
    fun homeScreen_showsFakeProductTitle() {
        composeRule.setContent {
            HomeScreen(
                onProductClick = {},
                onSearchClick = {},
            )
        }
        composeRule.onNodeWithText("Test Product").assertExists()
    }
}
