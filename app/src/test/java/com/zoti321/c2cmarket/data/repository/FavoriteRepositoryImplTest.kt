package com.zoti321.c2cmarket.data.repository

import com.zoti321.c2cmarket.data.local.dao.FavoriteDao
import com.zoti321.c2cmarket.data.local.entity.FavoriteEntity
import com.zoti321.c2cmarket.domain.model.Product
import com.zoti321.c2cmarket.domain.model.Rating
import com.zoti321.c2cmarket.domain.model.UserIds
import com.zoti321.c2cmarket.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FavoriteRepositoryImplTest {

    private lateinit var dao: FakeFavoriteDao
    private lateinit var repository: FavoriteRepositoryImpl

    private val product = Product(
        id = 42,
        title = "Watch",
        price = 49.0,
        description = "Desc",
        category = "jewelery",
        imageUrl = "https://example.com/w.png",
        rating = Rating(3.5, 5),
    )

    @Before
    fun setUp() {
        dao = FakeFavoriteDao()
        repository = FavoriteRepositoryImpl(
            dao,
            FakeAuthRepository(),
            FakeProductRepository(product),
        )
    }

    @Test
    fun toggleFavorite_addsWhenNotFavorite() = runTest {
        val result = repository.toggleFavorite(product)

        assertTrue(result.isSuccess)
        assertTrue(result.getOrThrow())
        assertTrue(dao.isFavorite(UserIds.GUEST, product.id))
    }

    @Test
    fun toggleFavorite_removesWhenFavorite() = runTest {
        repository.toggleFavorite(product)
        val result = repository.toggleFavorite(product)

        assertTrue(result.isSuccess)
        assertFalse(result.getOrThrow())
        assertFalse(dao.isFavorite(UserIds.GUEST, product.id))
    }

    @Test
    fun observeFavorites_returnsProductsForCurrentUser() = runTest {
        repository.toggleFavorite(product)

        val favorites = repository.observeFavorites().first()

        assertEquals(1, favorites.size)
        assertEquals(product.id, favorites.first().id)
    }
}

private class FakeProductRepository(
    private val product: Product,
) : ProductRepository {
    override fun pagingProducts(pageSize: Int, sort: String): Flow<androidx.paging.PagingData<Product>> =
        kotlinx.coroutines.flow.flowOf(androidx.paging.PagingData.empty())

    override suspend fun getProduct(id: Int): Result<Product> =
        if (id == product.id) Result.success(product) else Result.failure(IllegalStateException())

    override suspend fun getCategories(): Result<List<String>> = Result.success(emptyList())

    override suspend fun getProductsByCategory(category: String): Result<List<Product>> = Result.success(emptyList())

    override suspend fun getAllProducts(): Result<List<Product>> = Result.success(listOf(product))

    override fun searchProducts(query: String): Flow<com.zoti321.c2cmarket.domain.model.SearchResult> =
        kotlinx.coroutines.flow.flowOf(com.zoti321.c2cmarket.domain.model.SearchResult.Idle)
}

private class FakeFavoriteDao : FavoriteDao {
    private val favorites = MutableStateFlow<List<FavoriteEntity>>(emptyList())

    override fun observeIsFavorite(userId: String, id: Int): Flow<Boolean> =
        favorites.map { list -> list.any { it.userId == userId && it.productId == id } }

    override suspend fun isFavorite(userId: String, id: Int): Boolean =
        favorites.value.any { it.userId == userId && it.productId == id }

    override fun observeAll(userId: String): Flow<List<FavoriteEntity>> =
        favorites.map { list -> list.filter { it.userId == userId } }

    override suspend fun insert(entity: FavoriteEntity) {
        favorites.value = favorites.value.filterNot {
            it.userId == entity.userId && it.productId == entity.productId
        } + entity
    }

    override suspend fun deleteByProductId(userId: String, id: Int) {
        favorites.value = favorites.value.filterNot { it.userId == userId && it.productId == id }
    }

    override suspend fun deleteByProductIdAllUsers(id: Int) {
        favorites.value = favorites.value.filterNot { it.productId == id }
    }

    override suspend fun migrateGuestFavorites(newUserId: String) {
        favorites.value = favorites.value.map {
            if (it.userId == UserIds.GUEST) it.copy(userId = newUserId) else it
        }
    }
}
