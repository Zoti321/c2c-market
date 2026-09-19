package com.zoti321.c2cmarket.data.repository

import com.zoti321.c2cmarket.data.local.dao.FavoriteDao
import com.zoti321.c2cmarket.data.local.entity.FavoriteEntity
import com.zoti321.c2cmarket.domain.model.Product
import com.zoti321.c2cmarket.domain.model.Rating
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
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
        repository = FavoriteRepositoryImpl(dao)
    }

    @Test
    fun toggleFavorite_addsWhenNotFavorite() = runTest {
        val result = repository.toggleFavorite(product)

        assertTrue(result.isSuccess)
        assertTrue(result.getOrThrow())
        assertTrue(dao.isFavorite(product.id))
    }

    @Test
    fun toggleFavorite_removesWhenFavorite() = runTest {
        repository.toggleFavorite(product)
        val result = repository.toggleFavorite(product)

        assertTrue(result.isSuccess)
        assertFalse(result.getOrThrow())
        assertFalse(dao.isFavorite(product.id))
    }
}

private class FakeFavoriteDao : FavoriteDao {
    private val favorites = MutableStateFlow<Set<Int>>(emptySet())

    override fun observeIsFavorite(id: Int): Flow<Boolean> =
        favorites.map { id in it }

    override suspend fun isFavorite(id: Int): Boolean = id in favorites.value

    override suspend fun insert(entity: FavoriteEntity) {
        favorites.value = favorites.value + entity.productId
    }

    override suspend fun deleteByProductId(id: Int) {
        favorites.value = favorites.value - id
    }
}
