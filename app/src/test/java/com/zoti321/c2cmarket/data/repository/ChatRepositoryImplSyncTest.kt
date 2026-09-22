package com.zoti321.c2cmarket.data.repository

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.zoti321.c2cmarket.data.firebase.buildConversationRemoteId
import com.zoti321.c2cmarket.data.local.InMemoryDatabaseFactory
import com.zoti321.c2cmarket.data.local.entity.ListingEntity
import com.zoti321.c2cmarket.data.local.entity.SyncStateValues
import com.zoti321.c2cmarket.domain.model.Product
import com.zoti321.c2cmarket.domain.model.ProductSource
import com.zoti321.c2cmarket.domain.model.Rating
import com.zoti321.c2cmarket.domain.model.UserIds
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class ChatRepositoryImplSyncTest {

    private lateinit var database: com.zoti321.c2cmarket.data.local.C2CDatabase
    private lateinit var chatRemote: FakeChatRemoteDataSource
    private lateinit var repository: ChatRepositoryImpl
    private lateinit var deps: RepositoryTestDeps

    @Before
    fun setUp() {
        deps = createRepositoryTestDeps().copy(
            firebaseAuth = FakeFirebaseAuthGateway(signedIn = true),
        )
        database = InMemoryDatabaseFactory.create()
        chatRemote = deps.chatRemote
        val context = ApplicationProvider.getApplicationContext<Application>()
        repository = createChatRepository(
            database = database,
            authRepository = FakeAuthRepository(initialUserId = UserIds.google("buyer-1")),
            applicationScope = deps.scope,
            ioDispatcher = deps.dispatcher,
            context = context,
            deps = deps,
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun sendMessage_realSeller_uploadsToRemote() = runTest(deps.dispatcher) {
        seedListingSeller(UserIds.google("seller-1"))
        val product = localListingProduct(-1)
        val conversation = repository.getOrCreateConversation(product).getOrThrow()

        repository.sendMessage(conversation.id, "hello").getOrThrow()

        assertEquals(1, chatRemote.uploadedMessages.size)
        val stored = database.messageDao().getPendingMessages()
        assertTrue(stored.isEmpty())
    }

    @Test
    fun sendMessage_mockSeller_skipsRemoteUpload() = runTest(deps.dispatcher) {
        val conversation = repository.getOrCreateConversation(remoteProduct()).getOrThrow()

        repository.sendMessage(conversation.id, "hello").getOrThrow()

        assertTrue(chatRemote.uploadedMessages.isEmpty())
    }

    @Test
    fun sendMessage_uploadFailure_marksPendingThenRetrySyncs() = runTest(deps.dispatcher) {
        chatRemote.uploadShouldFail = true
        seedListingSeller(UserIds.google("seller-1"))
        val conversation = repository.getOrCreateConversation(localListingProduct(-1)).getOrThrow()

        repository.sendMessage(conversation.id, "retry me").getOrThrow()

        val pending = database.messageDao().getPendingMessages()
        assertEquals(1, pending.size)
        assertEquals(SyncStateValues.PENDING, pending.first().syncState)

        chatRemote.uploadShouldFail = false
        repository.retryPendingUploads()

        val afterRetry = database.messageDao().getPendingMessages()
        assertTrue(afterRetry.isEmpty())
    }

    @Test
    fun resolveLocalConversationId_findsByRemoteId() = runTest(deps.dispatcher) {
        seedListingSeller(UserIds.google("seller-1"))
        val buyerId = UserIds.google("buyer-1")
        val product = localListingProduct(-1)
        val remoteId = buildConversationRemoteId(buyerId, UserIds.google("seller-1"), product.id)
        val conversation = repository.getOrCreateConversation(product).getOrThrow()

        val resolved = repository.resolveLocalConversationId(remoteId)

        assertEquals(conversation.id, resolved)
    }

    private suspend fun seedListingSeller(sellerId: String) {
        database.listingDao().insert(
            ListingEntity(
                catalogId = -1,
                title = "Local",
                price = 10.0,
                description = "d",
                category = "electronics",
                imageUri = "https://example.com/img.jpg",
                sellerId = sellerId,
                createdAt = 1L,
                updatedAt = 1L,
            ),
        )
    }

    private fun localListingProduct(id: Int) = Product(
        id = id,
        title = "Local",
        price = 10.0,
        description = "d",
        category = "electronics",
        imageUrl = "https://example.com/img.jpg",
        rating = Rating(0.0, 0),
        source = ProductSource.LOCAL_LISTING,
    )

    private fun remoteProduct() = Product(
        id = 1,
        title = "Phone",
        price = 10.0,
        description = "desc",
        category = "electronics",
        imageUrl = "https://example.com/a.png",
        rating = Rating(4.0, 10),
        source = ProductSource.REMOTE,
    )
}
