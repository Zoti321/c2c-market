package com.zoti321.c2cmarket.data.repository

import com.zoti321.c2cmarket.data.local.dao.CartDao
import com.zoti321.c2cmarket.data.local.entity.CartItemEntity
import com.zoti321.c2cmarket.domain.model.Product
import com.zoti321.c2cmarket.domain.model.Rating
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CartRepositoryImplTest {

    private lateinit var dao: FakeCartDao
    private lateinit var repository: CartRepositoryImpl

    private val product = Product(
        id = 1,
        title = "Phone",
        price = 99.0,
        description = "Desc",
        category = "electronics",
        imageUrl = "https://example.com/a.png",
        rating = Rating(4.0, 10),
    )

    @Before
    fun setUp() {
        dao = FakeCartDao()
        repository = CartRepositoryImpl(dao)
    }

    @Test
    fun addItem_insertsNewRow() = runTest {
        repository.addItem(product)

        val items = repository.observeItems().first()
        assertEquals(1, items.size)
        assertEquals(1, items.first().quantity)
    }

    @Test
    fun addItem_incrementsExistingQuantity() = runTest {
        repository.addItem(product)
        repository.addItem(product)

        val items = repository.observeItems().first()
        assertEquals(1, items.size)
        assertEquals(2, items.first().quantity)
    }

    @Test
    fun removeItem_deletesRow() = runTest {
        repository.addItem(product)
        repository.removeItem(product.id)

        assertTrue(repository.observeItems().first().isEmpty())
    }

    @Test
    fun clearAll_emptiesCart() = runTest {
        repository.addItem(product)
        repository.clearAll()

        assertTrue(repository.observeItems().first().isEmpty())
    }
}

private class FakeCartDao : CartDao {
    private val items = MutableStateFlow<List<CartItemEntity>>(emptyList())

    override fun observeAll(): Flow<List<CartItemEntity>> = items.asStateFlow()

    override suspend fun getByProductId(id: Int): CartItemEntity? =
        items.value.firstOrNull { it.productId == id }

    override suspend fun insert(item: CartItemEntity) {
        items.value = items.value.filterNot { it.productId == item.productId } + item
    }

    override suspend fun updateQuantity(id: Int, qty: Int) {
        items.value = items.value.map {
            if (it.productId == id) it.copy(quantity = qty) else it
        }
    }

    override suspend fun updateItem(id: Int, title: String, price: Double, imageUrl: String, qty: Int) {
        items.value = items.value.map {
            if (it.productId == id) {
                it.copy(title = title, unitPrice = price, imageUrl = imageUrl, quantity = qty)
            } else {
                it
            }
        }
    }

    override suspend fun deleteByProductId(id: Int) {
        items.value = items.value.filterNot { it.productId == id }
    }

    override suspend fun clearAll() {
        items.value = emptyList()
    }
}
