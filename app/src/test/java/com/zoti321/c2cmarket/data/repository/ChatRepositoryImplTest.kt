package com.zoti321.c2cmarket.data.repository

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.zoti321.c2cmarket.data.local.entity.MessageEntity
import com.zoti321.c2cmarket.data.local.InMemoryDatabaseFactory
import com.zoti321.c2cmarket.domain.GuestSession
import com.zoti321.c2cmarket.domain.model.Product
import com.zoti321.c2cmarket.domain.model.ProductSource
import com.zoti321.c2cmarket.domain.model.Rating
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
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

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class ChatRepositoryImplTest {

    private lateinit var database: com.zoti321.c2cmarket.data.local.C2CDatabase
    private lateinit var repository: ChatRepositoryImpl
    private lateinit var testScheduler: TestCoroutineScheduler
    private lateinit var testScope: CoroutineScope

    @Before
    fun setUp() {
        testScheduler = TestCoroutineScheduler()
        val testDispatcher = StandardTestDispatcher(testScheduler)
        database = InMemoryDatabaseFactory.create()
        testScope = CoroutineScope(SupervisorJob() + testDispatcher)
        val context = ApplicationProvider.getApplicationContext<Application>()
        repository = ChatRepositoryImpl(
            database = database,
            authRepository = FakeAuthRepository(),
            context = context,
            applicationScope = testScope,
            ioDispatcher = testDispatcher,
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun getOrCreateConversation_createsMockSellerForRemoteProduct() = runTest(testScheduler) {
        val conversation = repository.getOrCreateConversation(remoteProduct()).getOrThrow()

        assertEquals("mock-seller-1", conversation.sellerId)
        assertEquals("卖家 · 电子产品", conversation.sellerDisplayName)
        assertEquals(GuestSession.GUEST_ID, conversation.buyerId)
    }

    @Test
    fun getOrCreateConversation_returnsSameConversationForSameProduct() = runTest(testScheduler) {
        val first = repository.getOrCreateConversation(remoteProduct()).getOrThrow()
        val second = repository.getOrCreateConversation(remoteProduct()).getOrThrow()

        assertEquals(first.id, second.id)
    }

    @Test
    fun saveDraft_and_observeDraft_roundtrip() = runTest(testScheduler) {
        val conversation = repository.getOrCreateConversation(remoteProduct()).getOrThrow()

        repository.saveDraft(conversation.id, "draft text")

        assertEquals("draft text", repository.observeDraft(conversation.id).first())
    }

    @Test
    fun saveDraft_emptyBody_clearsDraft() = runTest(testScheduler) {
        val conversation = repository.getOrCreateConversation(remoteProduct()).getOrThrow()
        repository.saveDraft(conversation.id, "draft text")

        repository.saveDraft(conversation.id, "")

        assertEquals("", repository.observeDraft(conversation.id).first())
    }

    @Test
    fun sendMessage_insertsSentMessageAndClearsDraft() = runTest(testScheduler) {
        val conversation = repository.getOrCreateConversation(remoteProduct()).getOrThrow()
        repository.saveDraft(conversation.id, "draft text")

        val message = repository.sendMessage(conversation.id, "hello").getOrThrow()

        assertEquals("hello", message.body)
        assertEquals("", repository.observeDraft(conversation.id).first())
        val messages = repository.observeMessages(conversation.id).first()
        assertEquals(1, messages.count { it.senderId == GuestSession.GUEST_ID })
        assertEquals("hello", messages.first { it.senderId == GuestSession.GUEST_ID }.body)
    }

    @Test
    fun sendMessage_blankBody_returnsFailure() = runTest(testScheduler) {
        val conversation = repository.getOrCreateConversation(remoteProduct()).getOrThrow()

        val result = repository.sendMessage(conversation.id, "   ")

        assertTrue(result.isFailure)
    }

    @Test
    fun markConversationRead_clearsUnreadAndMarksSellerMessagesRead() = runTest(testScheduler) {
        val conversation = repository.getOrCreateConversation(remoteProduct()).getOrThrow()
        val sellerReplyAt = System.currentTimeMillis()
        database.messageDao().insert(
            MessageEntity(
                conversationId = conversation.id,
                senderId = conversation.sellerId,
                body = "seller reply",
                status = "SENT",
                sentAt = sellerReplyAt,
                isRead = false,
            ),
        )
        database.conversationDao().updatePreview(
            id = conversation.id,
            preview = "seller reply",
            lastMessageAt = sellerReplyAt,
        )
        database.conversationDao().incrementBuyerUnread(conversation.id)

        repository.markConversationRead(conversation.id)

        val updated = repository.observeConversations().first().first()
        assertEquals(0, updated.unreadCount)
        val messages = repository.observeMessages(conversation.id).first()
        assertTrue(messages.any { it.senderId == conversation.sellerId && it.isRead })
    }

    @Test
    fun mockAutoReply_insertsSellerMessageAfterDelay() = runTest(testScheduler) {
        val conversation = repository.getOrCreateConversation(remoteProduct()).getOrThrow()

        repository.sendMessage(conversation.id, "hello").getOrThrow()
        advanceTimeBy(ChatRepositoryImpl.MOCK_SELLER_REPLY_DELAY_MS)
        advanceUntilIdle()

        val messages = repository.observeMessages(conversation.id).first()
        assertEquals(2, messages.size)
        assertEquals(conversation.sellerId, messages.last().senderId)
        assertFalse(messages.last().isRead)

        val updated = repository.observeConversations().first().first()
        assertEquals(1, updated.unreadCount)
        assertTrue(updated.lastMessagePreview.isNotEmpty())
    }

    @Test
    fun sellerSendMessage_incrementsBuyerUnread() = runTest(testScheduler) {
        val sellerId = "google:seller-1"
        val auth = FakeAuthRepository(initialUserId = sellerId)
        val sellerRepo = ChatRepositoryImpl(
            database = database,
            authRepository = auth,
            context = ApplicationProvider.getApplicationContext(),
            applicationScope = testScope,
            ioDispatcher = StandardTestDispatcher(testScheduler),
        )
        val buyerAuth = FakeAuthRepository()
        val buyerRepo = ChatRepositoryImpl(
            database = database,
            authRepository = buyerAuth,
            context = ApplicationProvider.getApplicationContext(),
            applicationScope = testScope,
            ioDispatcher = StandardTestDispatcher(testScheduler),
        )
        val listingProduct = localListingProduct(sellerId = sellerId)
        database.listingDao().insert(
            com.zoti321.c2cmarket.data.local.entity.ListingEntity(
                catalogId = listingProduct.id,
                title = listingProduct.title,
                price = listingProduct.price,
                description = listingProduct.description,
                category = listingProduct.category,
                imageUri = listingProduct.imageUrl,
                sellerId = sellerId,
                createdAt = 1L,
                updatedAt = 1L,
            ),
        )
        val conversation = buyerRepo.getOrCreateConversation(listingProduct).getOrThrow()

        sellerRepo.sendMessage(conversation.id, "seller reply").getOrThrow()
        advanceUntilIdle()

        val buyerView = buyerRepo.observeConversationsAsBuyer().first().first()
        assertEquals(1, buyerView.unreadCount)
        assertEquals(0, buyerView.sellerUnreadCount)
    }

    @Test
    fun sellerSendMessage_doesNotTriggerMockReply() = runTest(testScheduler) {
        val sellerId = "google:seller-1"
        val auth = FakeAuthRepository(initialUserId = sellerId)
        val sellerRepo = ChatRepositoryImpl(
            database = database,
            authRepository = auth,
            context = ApplicationProvider.getApplicationContext(),
            applicationScope = testScope,
            ioDispatcher = StandardTestDispatcher(testScheduler),
        )
        val buyerAuth = FakeAuthRepository()
        val buyerRepo = ChatRepositoryImpl(
            database = database,
            authRepository = buyerAuth,
            context = ApplicationProvider.getApplicationContext(),
            applicationScope = testScope,
            ioDispatcher = StandardTestDispatcher(testScheduler),
        )
        val listingProduct = localListingProduct(sellerId = sellerId)
        database.listingDao().insert(
            com.zoti321.c2cmarket.data.local.entity.ListingEntity(
                catalogId = listingProduct.id,
                title = listingProduct.title,
                price = listingProduct.price,
                description = listingProduct.description,
                category = listingProduct.category,
                imageUri = listingProduct.imageUrl,
                sellerId = sellerId,
                createdAt = 1L,
                updatedAt = 1L,
            ),
        )
        val conversation = buyerRepo.getOrCreateConversation(listingProduct).getOrThrow()
        buyerRepo.sendMessage(conversation.id, "buyer hello").getOrThrow()

        sellerRepo.sendMessage(conversation.id, "seller reply").getOrThrow()
        advanceTimeBy(ChatRepositoryImpl.MOCK_SELLER_REPLY_DELAY_MS + 100)
        advanceUntilIdle()

        val messages = buyerRepo.observeMessages(conversation.id).first()
        assertEquals(2, messages.size)
    }

    @Test
    fun observeConversationsAsSeller_returnsSellerInbox() = runTest(testScheduler) {
        val sellerId = "google:seller-1"
        val buyerAuth = FakeAuthRepository()
        val buyerRepo = ChatRepositoryImpl(
            database = database,
            authRepository = buyerAuth,
            context = ApplicationProvider.getApplicationContext(),
            applicationScope = testScope,
            ioDispatcher = StandardTestDispatcher(testScheduler),
        )
        val sellerAuth = FakeAuthRepository(initialUserId = sellerId)
        val sellerRepo = ChatRepositoryImpl(
            database = database,
            authRepository = sellerAuth,
            context = ApplicationProvider.getApplicationContext(),
            applicationScope = testScope,
            ioDispatcher = StandardTestDispatcher(testScheduler),
        )
        val listingProduct = localListingProduct(sellerId = sellerId)
        database.listingDao().insert(
            com.zoti321.c2cmarket.data.local.entity.ListingEntity(
                catalogId = listingProduct.id,
                title = listingProduct.title,
                price = listingProduct.price,
                description = listingProduct.description,
                category = listingProduct.category,
                imageUri = listingProduct.imageUrl,
                sellerId = sellerId,
                createdAt = 1L,
                updatedAt = 1L,
            ),
        )
        val conversation = buyerRepo.getOrCreateConversation(listingProduct).getOrThrow()
        buyerRepo.sendMessage(conversation.id, "question").getOrThrow()
        advanceUntilIdle()

        val sellerInbox = sellerRepo.observeConversationsAsSeller().first()
        assertEquals(1, sellerInbox.size)
        assertEquals(1, sellerInbox.first().sellerUnreadCount)
    }

    private fun localListingProduct(sellerId: String) = Product(
        id = -1,
        title = "Local Listing",
        price = 50.0,
        description = "Desc",
        category = "electronics",
        imageUrl = "content://image",
        rating = Rating(rate = 0.0, count = 0),
        source = ProductSource.LOCAL_LISTING,
    )

    private fun remoteProduct(id: Int = 1) = Product(
        id = id,
        title = "Test Product",
        price = 9.99,
        description = "Desc",
        category = "electronics",
        imageUrl = "https://example.com/img.jpg",
        rating = Rating(rate = 4.5, count = 100),
        source = ProductSource.REMOTE,
    )
}
