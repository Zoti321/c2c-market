package com.zoti321.c2cmarket.data.migration

import com.zoti321.c2cmarket.data.local.InMemoryDatabaseFactory
import com.zoti321.c2cmarket.data.local.entity.ConversationEntity
import com.zoti321.c2cmarket.data.local.entity.ListingEntity
import com.zoti321.c2cmarket.data.local.entity.OrderEntity
import com.zoti321.c2cmarket.domain.model.OrderStatus
import com.zoti321.c2cmarket.domain.model.UserIds
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class GuestDataMigratorTest {

    private lateinit var database: com.zoti321.c2cmarket.data.local.C2CDatabase
    private lateinit var migrator: GuestDataMigrator

    @Before
    fun setUp() {
        database = InMemoryDatabaseFactory.create()
        migrator = GuestDataMigrator(
            database = database,
            orderDao = database.orderDao(),
            listingDao = database.listingDao(),
            conversationDao = database.conversationDao(),
            cartDao = database.cartDao(),
            favoriteDao = database.favoriteDao(),
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun migrateGuestDataTo_updatesGuestOwnedRows() = runTest {
        val googleUserId = UserIds.google("sub-1")
        database.orderDao().insertOrder(
            OrderEntity(
                guestId = UserIds.GUEST,
                totalAmount = 10.0,
                status = OrderStatus.COMPLETED.name,
                createdAt = 1L,
            ),
        )
        database.listingDao().insert(
            ListingEntity(
                catalogId = -1,
                title = "Listing",
                price = 5.0,
                description = "Desc",
                category = "electronics",
                imageUri = "content://image",
                sellerId = UserIds.GUEST,
                createdAt = 1L,
                updatedAt = 1L,
            ),
        )
        database.conversationDao().insert(
            ConversationEntity(
                productId = 1,
                productTitle = "Phone",
                productImageUrl = "https://example.com/a.png",
                sellerId = "mock-seller-1",
                sellerDisplayName = "Seller",
                buyerId = UserIds.GUEST,
                lastMessagePreview = "",
                lastMessageAt = 1L,
                unreadCount = 0,
                createdAt = 1L,
            ),
        )

        migrator.migrateGuestDataTo(googleUserId)

        val order = database.orderDao().observeAllOrders().first().first()
        assertEquals(googleUserId, order.guestId)

        val listing = database.listingDao().observeAll().first().first()
        assertEquals(googleUserId, listing.sellerId)

        val conversation = database.conversationDao().observeByBuyerId(googleUserId).first().first()
        assertEquals(googleUserId, conversation.buyerId)
    }

    @Test
    fun migrateGuestDataTo_migratesCartAndFavorites() = runTest {
        val googleUserId = UserIds.google("sub-cart")
        database.cartDao().insert(
            com.zoti321.c2cmarket.data.local.entity.CartItemEntity(
                userId = UserIds.GUEST,
                productId = 1,
                title = "Phone",
                unitPrice = 9.9,
                imageUrl = "img",
                quantity = 1,
                addedAt = 1L,
            ),
        )
        database.favoriteDao().insert(
            com.zoti321.c2cmarket.data.local.entity.FavoriteEntity(
                userId = UserIds.GUEST,
                productId = 2,
                createdAt = 1L,
            ),
        )

        migrator.migrateGuestDataTo(googleUserId)

        assertTrue(database.cartDao().observeAll(googleUserId).first().isNotEmpty())
        assertTrue(database.favoriteDao().observeAll(googleUserId).first().isNotEmpty())
        assertTrue(database.cartDao().observeAll(UserIds.GUEST).first().isEmpty())
    }

    @Test
    fun migrateGuestDataTo_isIdempotentWhenNoGuestRows() = runTest {
        val googleUserId = UserIds.google("sub-2")
        database.orderDao().insertOrder(
            OrderEntity(
                guestId = googleUserId,
                totalAmount = 10.0,
                status = OrderStatus.COMPLETED.name,
                createdAt = 1L,
            ),
        )

        migrator.migrateGuestDataTo(googleUserId)

        val order = database.orderDao().observeAllOrders().first().first()
        assertEquals(googleUserId, order.guestId)
    }
}
