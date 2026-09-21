package com.zoti321.c2cmarket.data.repository

import com.zoti321.c2cmarket.data.local.dao.CartDao
import com.zoti321.c2cmarket.data.local.entity.CartItemEntity
import com.zoti321.c2cmarket.domain.model.Product
import com.zoti321.c2cmarket.domain.model.Rating
import com.zoti321.c2cmarket.domain.model.UserIds
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
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
        repository = CartRepositoryImpl(dao, FakeAuthRepository())
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

    @Test
    fun observeItems_isolatedByUser() = runTest {
        val auth = FakeAuthRepository()
        val guestRepo = CartRepositoryImpl(dao, auth)
        guestRepo.addItem(product)

        auth.setSignedIn(
            com.zoti321.c2cmarket.domain.model.UserProfile(
                userId = UserIds.google("other"),
                displayName = "Other",
                email = null,
                photoUrl = null,
            ),
        )

        assertTrue(guestRepo.observeItems().first().isEmpty())
    }
}

private class FakeCartDao : CartDao {
    private val items = MutableStateFlow<List<CartItemEntity>>(emptyList())

    override fun observeAll(userId: String): Flow<List<CartItemEntity>> =
        items.map { list -> list.filter { it.userId == userId } }

    override suspend fun getByProductId(userId: String, id: Int): CartItemEntity? =
        items.value.firstOrNull { it.userId == userId && it.productId == id }

    override suspend fun insert(item: CartItemEntity) {
        items.value = items.value.filterNot {
            it.userId == item.userId && it.productId == item.productId
        } + item
    }

    override suspend fun updateQuantity(userId: String, id: Int, qty: Int) {
        items.value = items.value.map {
            if (it.userId == userId && it.productId == id) it.copy(quantity = qty) else it
        }
    }

    override suspend fun updateItem(userId: String, id: Int, title: String, price: Double, imageUrl: String, qty: Int) {
        items.value = items.value.map {
            if (it.userId == userId && it.productId == id) {
                it.copy(title = title, unitPrice = price, imageUrl = imageUrl, quantity = qty)
            } else {
                it
            }
        }
    }

    override suspend fun deleteByProductId(userId: String, id: Int) {
        items.value = items.value.filterNot { it.userId == userId && it.productId == id }
    }

    override suspend fun deleteByProductIdAllUsers(id: Int) {
        items.value = items.value.filterNot { it.productId == id }
    }

    override suspend fun clearForUser(userId: String) {
        items.value = items.value.filterNot { it.userId == userId }
    }

    override suspend fun migrateGuestCart(newUserId: String) {
        items.value = items.value.map {
            if (it.userId == UserIds.GUEST) it.copy(userId = newUserId) else it
        }
    }
}
