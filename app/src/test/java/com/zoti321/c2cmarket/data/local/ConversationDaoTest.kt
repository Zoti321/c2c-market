package com.zoti321.c2cmarket.data.local

import com.zoti321.c2cmarket.data.local.entity.ConversationEntity
import com.zoti321.c2cmarket.domain.GuestSession
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class ConversationDaoTest {

    private lateinit var database: C2CDatabase

    @Before
    fun setUp() {
        database = InMemoryDatabaseFactory.create()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun observeBySellerId_returnsMatchingConversations() = runTest {
        val dao = database.conversationDao()
        val sellerId = "google:seller-1"
        insertConversation(buyerId = GuestSession.GUEST_ID, sellerId = sellerId, productId = 1)
        insertConversation(buyerId = GuestSession.GUEST_ID, sellerId = "other-seller", productId = 2)

        val sellerInbox = dao.observeBySellerId(sellerId).first()

        assertEquals(1, sellerInbox.size)
        assertEquals(sellerId, sellerInbox.first().sellerId)
    }

    @Test
    fun observeByBuyerId_returnsMatchingConversations() = runTest {
        val dao = database.conversationDao()
        insertConversation(buyerId = GuestSession.GUEST_ID, sellerId = "seller-a", productId = 1)
        insertConversation(buyerId = "google:buyer-2", sellerId = "seller-b", productId = 2)

        val buyerInbox = dao.observeByBuyerId(GuestSession.GUEST_ID).first()

        assertEquals(1, buyerInbox.size)
        assertEquals(GuestSession.GUEST_ID, buyerInbox.first().buyerId)
    }

    @Test
    fun sellerUnreadCounters_incrementAndClear() = runTest {
        val dao = database.conversationDao()
        val conversationId = insertConversation(
            buyerId = GuestSession.GUEST_ID,
            sellerId = "google:seller-1",
            productId = 3,
        )

        dao.incrementSellerUnread(conversationId)
        dao.incrementSellerUnread(conversationId)
        assertEquals(2, dao.getById(conversationId)?.sellerUnreadCount)

        dao.clearSellerUnread(conversationId)
        assertEquals(0, dao.getById(conversationId)?.sellerUnreadCount)
    }

    private suspend fun insertConversation(
        buyerId: String,
        sellerId: String,
        productId: Int,
    ): Long = database.conversationDao().insert(
        ConversationEntity(
            productId = productId,
            productTitle = "Product $productId",
            productImageUrl = "https://example.com/$productId.jpg",
            sellerId = sellerId,
            sellerDisplayName = "卖家",
            buyerId = buyerId,
            buyerDisplayName = "买家",
            lastMessagePreview = "",
            lastMessageAt = productId.toLong(),
            unreadCount = 0,
            sellerUnreadCount = 0,
            createdAt = 1L,
        ),
    )
}
