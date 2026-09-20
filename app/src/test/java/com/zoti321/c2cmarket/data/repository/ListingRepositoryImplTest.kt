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
            orderDao = database.orderDao(),
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
                userId = "guest",
                productId = listing.id,
                title = listing.title,
                unitPrice = listing.price,
                imageUrl = listing.imageUrl,
                quantity = 1,
                addedAt = 1L,
            ),
        )
        database.favoriteDao().insert(FavoriteEntity("guest", listing.id, 1L))

        repository.delete(listing.id)

        assertTrue(database.listingDao().getByCatalogId(listing.id) == null)
        assertTrue(database.cartDao().getByProductId("guest", listing.id) == null)
        assertFalse(database.favoriteDao().isFavorite("guest", listing.id))
    }

    @Test
    fun create_persistsMeetupLocation() = runTest {
        val listing = repository.create(
            validInput("Meetup Item").copy(meetupLocation = "深圳湾公园"),
        ).getOrThrow()

        val stored = database.listingDao().getByCatalogId(listing.id)

        assertEquals("深圳湾公园", stored?.meetupLocation)
        assertEquals("深圳湾公园", repository.getProductByCatalogId(listing.id).getOrThrow().meetupLocation)
    }

    @Test
    fun observeByCategory_filtersListings() = runTest {
        repository.create(validInput("Phone", category = "electronics"))
        repository.create(validInput("Ring", category = "jewelery"))

        val electronics = repository.observeByCategory("electronics").first()

        assertEquals(1, electronics.size)
        assertEquals("Phone", electronics.first().title)
    }

    @Test
    fun updateStatus_changesListingStatus() = runTest {
        val listing = repository.create(validInput("Status Item")).getOrThrow()

        repository.updateStatus(listing.id, com.zoti321.c2cmarket.domain.model.ListingStatus.SOLD)

        val stored = database.listingDao().getByCatalogId(listing.id)
        assertEquals("SOLD", stored?.status)
    }

    @Test
    fun observeAsProducts_excludesSoldListings() = runTest {
        val listing = repository.create(validInput("Hidden When Sold")).getOrThrow()
        repository.updateStatus(listing.id, com.zoti321.c2cmarket.domain.model.ListingStatus.SOLD)

        val public = repository.observeAsProducts().first()

        assertTrue(public.none { it.id == listing.id })
    }

    @Test
    fun delete_reservedListing_fails() = runTest {
        val listing = repository.create(validInput("Reserved Item")).getOrThrow()
        database.listingDao().updateStatus(listing.id, "RESERVED", System.currentTimeMillis())

        val result = repository.delete(listing.id)

        assertTrue(result.isFailure)
    }

    private fun validInput(title: String, category: String = "electronics") = ListingInput(
        title = title,
        price = 9.99,
        description = "Test listing",
        category = category,
        imageUri = "content://test/image",
    )
}
