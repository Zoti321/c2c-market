package com.zoti321.c2cmarket.data.repository

import android.content.Context
import com.zoti321.c2cmarket.R
import com.zoti321.c2cmarket.data.firebase.FirebaseAuthGateway
import com.zoti321.c2cmarket.data.firebase.buildConversationRemoteId
import com.zoti321.c2cmarket.data.local.C2CDatabase
import com.zoti321.c2cmarket.data.local.dao.ConversationDao
import com.zoti321.c2cmarket.data.local.dao.ListingDao
import com.zoti321.c2cmarket.data.local.dao.MessageDao
import com.zoti321.c2cmarket.data.local.dao.observeDraft
import com.zoti321.c2cmarket.data.local.entity.MessageEntity
import com.zoti321.c2cmarket.data.local.entity.SyncStateValues
import com.zoti321.c2cmarket.data.mapper.toDomain
import com.zoti321.c2cmarket.data.mapper.toEntityValue
import com.zoti321.c2cmarket.data.sync.RemoteSyncGateway
import com.zoti321.c2cmarket.di.ApplicationScope
import com.zoti321.c2cmarket.di.IoDispatcher
import com.zoti321.c2cmarket.domain.MockSellerResolver
import com.zoti321.c2cmarket.domain.datasource.ChatRemoteDataSource
import com.zoti321.c2cmarket.domain.model.AuthState
import com.zoti321.c2cmarket.domain.model.Conversation
import com.zoti321.c2cmarket.domain.model.Message
import com.zoti321.c2cmarket.domain.model.MessageStatus
import com.zoti321.c2cmarket.domain.model.Product
import com.zoti321.c2cmarket.domain.model.ProductSource
import com.zoti321.c2cmarket.domain.model.UserIds
import com.zoti321.c2cmarket.domain.repository.AuthRepository
import com.zoti321.c2cmarket.domain.repository.ChatRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
@Singleton
class ChatRepositoryImpl @Inject constructor(
    database: C2CDatabase,
    private val authRepository: AuthRepository,
    private val chatRemote: ChatRemoteDataSource,
    private val firebaseAuthGateway: FirebaseAuthGateway,
    private val remoteSyncGateway: RemoteSyncGateway,
    @ApplicationContext private val context: Context,
    @ApplicationScope private val applicationScope: CoroutineScope,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : ChatRepository {

    private val conversationDao: ConversationDao = database.conversationDao()
    private val messageDao: MessageDao = database.messageDao()
    private val listingDao: ListingDao = database.listingDao()

    override fun observeConversationsAsBuyer(): Flow<List<Conversation>> =
        authRepository.currentUserId().flatMapLatest { buyerId ->
            conversationDao.observeByBuyerId(buyerId).map { entities ->
                entities.map { it.toDomain() }
            }
        }

    override fun observeConversationsAsSeller(): Flow<List<Conversation>> =
        authRepository.currentUserId().flatMapLatest { sellerId ->
            conversationDao.observeBySellerId(sellerId).map { entities ->
                entities.map { it.toDomain() }
            }
        }

    override fun observeConversationForCurrentUser(conversationId: Long): Flow<Conversation?> =
        authRepository.currentUserId().flatMapLatest { userId ->
            conversationDao.observeById(conversationId).map { entity ->
                entity?.takeIf { it.buyerId == userId || it.sellerId == userId }?.toDomain()
            }
        }

    override fun observeMessages(conversationId: Long): Flow<List<Message>> =
        messageDao.observeSent(conversationId).map { entities ->
            entities.map { it.toDomain() }
        }

    override suspend fun getConversationForUser(conversationId: Long): Conversation? =
        withContext(ioDispatcher) {
            val userId = authRepository.currentUserId().first()
            val entity = conversationDao.getById(conversationId) ?: return@withContext null
            if (entity.buyerId != userId && entity.sellerId != userId) return@withContext null
            entity.toDomain()
        }

    override suspend fun resolveLocalConversationId(remoteId: String): Long? =
        withContext(ioDispatcher) {
            conversationDao.findByRemoteId(remoteId)?.id
        }

    override fun startActiveConversationSync(conversationId: Long) {
        applicationScope.launch(ioDispatcher) {
            val entity = conversationDao.getById(conversationId) ?: return@launch
            val remoteId = entity.remoteId ?: buildConversationRemoteId(
                entity.buyerId,
                entity.sellerId,
                entity.productId,
            )
            if (!shouldUseRemote(entity.sellerId)) return@launch
            remoteSyncGateway.observeActiveConversationMessages(remoteId)
        }
    }

    override fun stopActiveConversationSync() {
        remoteSyncGateway.stopActiveConversationMessages()
    }

    override suspend fun getOrCreateConversation(product: Product): Result<Conversation> =
        withContext(ioDispatcher) {
            runCatching {
                val seller = resolveSeller(listingDao, product)
                    ?: error("Cannot create conversation without seller")
                val buyerId = authRepository.currentUserId().first()
                val buyerDisplayName = resolveBuyerDisplayName(authRepository)
                val existing = conversationDao.findByUniqueKey(
                    buyerId = buyerId,
                    sellerId = seller.sellerId,
                    productId = product.id,
                )
                if (existing != null) {
                    return@runCatching existing.toDomain()
                }
                val now = System.currentTimeMillis()
                val remoteId = buildConversationRemoteId(buyerId, seller.sellerId, product.id)
                val id = conversationDao.insert(
                    com.zoti321.c2cmarket.data.local.entity.ConversationEntity(
                        productId = product.id,
                        productTitle = product.title,
                        productImageUrl = product.imageUrl,
                        sellerId = seller.sellerId,
                        sellerDisplayName = seller.sellerDisplayName,
                        buyerId = buyerId,
                        buyerDisplayName = buyerDisplayName,
                        lastMessagePreview = "",
                        lastMessageAt = now,
                        unreadCount = 0,
                        sellerUnreadCount = 0,
                        createdAt = now,
                        remoteId = remoteId,
                    ),
                )
                val created = conversationDao.getById(id)?.toDomain()
                    ?: error("Failed to load created conversation")
                if (shouldUseRemote(seller.sellerId)) {
                    chatRemote.ensureConversation(created, remoteId)
                        .onFailure { /* best-effort */ }
                }
                created
            }
        }

    override suspend fun saveDraft(conversationId: Long, body: String) =
        withContext(ioDispatcher) {
            val senderId = authRepository.currentUserId().first()
            if (body.isEmpty()) {
                messageDao.deleteDraft(conversationId)
                return@withContext
            }
            val existing = messageDao.getDraft(conversationId)
            if (existing != null) {
                messageDao.update(existing.copy(body = body))
            } else {
                messageDao.insert(
                    MessageEntity(
                        conversationId = conversationId,
                        senderId = senderId,
                        body = body,
                        status = MessageStatus.DRAFT.toEntityValue(),
                        sentAt = null,
                        isRead = true,
                    ),
                )
            }
        }

    override suspend fun sendMessage(conversationId: Long, body: String): Result<Message> =
        withContext(ioDispatcher) {
            runCatching {
                val trimmed = body.trim()
                require(trimmed.isNotEmpty()) { "Message body is blank" }
                require(trimmed.length <= MAX_MESSAGE_LENGTH) { "Message too long" }

                val conversation = conversationDao.getById(conversationId)
                    ?: error("Conversation not found")
                val senderId = authRepository.currentUserId().first()

                messageDao.deleteDraft(conversationId)
                val now = System.currentTimeMillis()
                val remoteId = UUID.randomUUID().toString()
                val messageId = messageDao.insert(
                    MessageEntity(
                        conversationId = conversationId,
                        senderId = senderId,
                        body = trimmed,
                        status = MessageStatus.SENT.toEntityValue(),
                        sentAt = now,
                        isRead = true,
                        remoteId = remoteId,
                        syncState = SyncStateValues.PENDING,
                    ),
                )
                conversationDao.updatePreview(
                    id = conversationId,
                    preview = trimmed,
                    lastMessageAt = now,
                )

                when (senderId) {
                    conversation.buyerId -> {
                        conversationDao.incrementSellerUnread(conversationId)
                        if (conversation.sellerId.startsWith(MOCK_SELLER_PREFIX)) {
                            scheduleMockReply(
                                conversationDao = conversationDao,
                                messageDao = messageDao,
                                context = context,
                                applicationScope = applicationScope,
                                ioDispatcher = ioDispatcher,
                                conversationId = conversationId,
                                sellerId = conversation.sellerId,
                            )
                        }
                    }
                    conversation.sellerId -> {
                        conversationDao.incrementBuyerUnread(conversationId)
                    }
                }

                val message = MessageEntity(
                    id = messageId,
                    conversationId = conversationId,
                    senderId = senderId,
                    body = trimmed,
                    status = MessageStatus.SENT.toEntityValue(),
                    sentAt = now,
                    isRead = true,
                    remoteId = remoteId,
                    syncState = SyncStateValues.PENDING,
                ).toDomain()

                if (shouldUseRemote(conversation.sellerId)) {
                    uploadMessage(conversation.toDomain(), message, remoteId)
                } else {
                    messageDao.updateRemoteSync(messageId, remoteId, SyncStateValues.SYNCED)
                }

                message
            }
        }

    override suspend fun markConversationRead(conversationId: Long) =
        withContext(ioDispatcher) {
            val userId = authRepository.currentUserId().first()
            val conversation = conversationDao.getById(conversationId) ?: return@withContext
            messageDao.markOtherPartyMessagesRead(conversationId, userId)
            when (userId) {
                conversation.buyerId -> conversationDao.clearBuyerUnread(conversationId)
                conversation.sellerId -> conversationDao.clearSellerUnread(conversationId)
            }
        }

    override fun observeDraft(conversationId: Long): Flow<String> =
        messageDao.observeDraft(conversationId)

    override suspend fun retryPendingUploads() = withContext(ioDispatcher) {
        if (!firebaseAuthGateway.isSignedIn()) return@withContext
        val pending = messageDao.getPendingMessages()
        pending.forEach { entity ->
            val conversation = conversationDao.getById(entity.conversationId) ?: return@forEach
            if (!shouldUseRemote(conversation.sellerId)) return@forEach
            val remoteId = entity.remoteId ?: return@forEach
            val message = entity.toDomain()
            uploadMessage(conversation.toDomain(), message, remoteId)
        }
    }

    private suspend fun uploadMessage(conversation: Conversation, message: Message, remoteId: String) {
        val conversationRemoteId = conversation.remoteId
            ?: buildConversationRemoteId(conversation.buyerId, conversation.sellerId, conversation.productId)
        chatRemote.uploadMessage(conversationRemoteId, message, remoteId)
            .onSuccess {
                messageDao.updateRemoteSync(message.id, remoteId, SyncStateValues.SYNCED)
            }
            .onFailure {
                messageDao.updateRemoteSync(message.id, remoteId, SyncStateValues.PENDING)
                scheduleRetry()
            }
    }

    private fun scheduleRetry() {
        applicationScope.launch(ioDispatcher) {
            delay(RETRY_DELAY_MS)
            retryPendingUploads()
        }
    }

    private suspend fun shouldUseRemote(sellerId: String): Boolean =
        firebaseAuthGateway.isSignedIn() &&
            authRepository.currentUserId().first() != UserIds.GUEST &&
            !sellerId.startsWith(MOCK_SELLER_PREFIX)

    companion object {
        const val MOCK_SELLER_REPLY_DELAY_MS = 3_000L
        const val MAX_MESSAGE_LENGTH = 500
        private const val MOCK_SELLER_PREFIX = "mock-seller-"
        private const val RETRY_DELAY_MS = 5_000L
    }
}

private suspend fun resolveBuyerDisplayName(authRepository: AuthRepository): String =
    when (val state = authRepository.observeAuthState().first()) {
        AuthState.Guest -> "游客"
        is AuthState.SignedIn -> state.profile.displayName
    }

private suspend fun resolveSeller(
    listingDao: ListingDao,
    product: Product,
): MockSellerResolver.MockSeller? =
    when (product.source) {
        ProductSource.LOCAL_LISTING -> listingDao.getByCatalogId(product.id)?.let { listing ->
            MockSellerResolver.MockSeller(
                sellerId = listing.sellerId,
                sellerDisplayName = "挂牌卖家",
            )
        }
        else -> MockSellerResolver.resolve(product)
    }

private fun scheduleMockReply(
    conversationDao: ConversationDao,
    messageDao: MessageDao,
    context: Context,
    applicationScope: CoroutineScope,
    ioDispatcher: CoroutineDispatcher,
    conversationId: Long,
    sellerId: String,
) {
    applicationScope.launch(ioDispatcher) {
        delay(ChatRepositoryImpl.MOCK_SELLER_REPLY_DELAY_MS)
        if (conversationDao.getById(conversationId) == null) return@launch
        val body = context.getString(R.string.chat_mock_reply_default)
        val now = System.currentTimeMillis()
        messageDao.insert(
            MessageEntity(
                conversationId = conversationId,
                senderId = sellerId,
                body = body,
                status = MessageStatus.SENT.toEntityValue(),
                sentAt = now,
                isRead = false,
            ),
        )
        conversationDao.updatePreview(
            id = conversationId,
            preview = body,
            lastMessageAt = now,
        )
        conversationDao.incrementBuyerUnread(conversationId)
    }
}
