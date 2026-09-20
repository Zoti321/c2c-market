package com.zoti321.c2cmarket.data.repository

import com.zoti321.c2cmarket.data.local.InMemoryDatabaseFactory
import com.zoti321.c2cmarket.data.local.entity.FavoriteEntity
import com.zoti321.c2cmarket.domain.model.ListingInput
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class ListingRepositoryImplTest {

    private lateinit var database: com.zoti321.c2cmarket.data.local.C2CDatabase
    private lateinit var repository: ListingRepositoryImpl

    @Before
    fun setUp() {
        database = InMemoryDatabaseFactory.create()
        repository = ListingRepositoryImpl(
            database = database,
            listingDao = database.listingDao(),
            cartDao = database.cartDao(),
            favoriteDao = database.favoriteDao(),
            authRepository = FakeAuthRepository(),
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun create_assignsNegativeCatalogIds() = runTest {
        val first = repository.create(validInput("First")).getOrThrow()
        val second = repository.create(validInput("Second")).getOrThrow()

        assertEquals(-1, first.id)
        assertEquals(-2, second.id)
    }

    @Test
    fun delete_removesCartAndFavoriteRows() = runTest {
        val listing = repository.create(validInput("Delete Me")).getOrThrow()
        database.cartDao().insert(
            com.zoti321.c2cmarket.data.local.entity.CartItemEntity(
                productId = listing.id,
                title = listing.title,
                unitPrice = listing.price,
                imageUrl = listing.imageUrl,
                quantity = 1,
                addedAt = 1L,
            ),
        )
        database.favoriteDao().insert(FavoriteEntity(listing.id, 1L))

        repository.delete(listing.id)

        assertTrue(database.listingDao().getByCatalogId(listing.id) == null)
        assertTrue(database.cartDao().getByProductId(listing.id) == null)
        assertFalse(database.favoriteDao().isFavorite(listing.id))
    }

    @Test
    fun observeByCategory_filtersListings() = runTest {
        repository.create(validInput("Phone", category = "electronics"))
        repository.create(validInput("Ring", category = "jewelery"))

        val electronics = repository.observeByCategory("electronics").first()

        assertEquals(1, electronics.size)
        assertEquals("Phone", electronics.first().title)
    }

    private fun validInput(title: String, category: String = "electronics") = ListingInput(
        title = title,
        price = 9.99,
        description = "Test listing",
        category = category,
        imageUri = "content://test/image",
    )
}
