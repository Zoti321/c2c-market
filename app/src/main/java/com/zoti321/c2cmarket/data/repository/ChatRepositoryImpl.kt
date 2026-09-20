package com.zoti321.c2cmarket.data.repository

import android.content.Context
import com.zoti321.c2cmarket.R
import com.zoti321.c2cmarket.data.local.dao.ConversationDao
import com.zoti321.c2cmarket.data.local.dao.MessageDao
import com.zoti321.c2cmarket.data.local.dao.observeDraft
import com.zoti321.c2cmarket.data.local.entity.MessageEntity
import com.zoti321.c2cmarket.data.mapper.toDomain
import com.zoti321.c2cmarket.data.mapper.toEntityValue
import com.zoti321.c2cmarket.di.ApplicationScope
import com.zoti321.c2cmarket.di.IoDispatcher
import com.zoti321.c2cmarket.domain.GuestSession
import com.zoti321.c2cmarket.domain.MockSellerResolver
import com.zoti321.c2cmarket.domain.model.Conversation
import com.zoti321.c2cmarket.domain.model.Message
import com.zoti321.c2cmarket.domain.model.MessageStatus
import com.zoti321.c2cmarket.domain.model.Product
import com.zoti321.c2cmarket.domain.repository.ChatRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Singleton
class ChatRepositoryImpl @Inject constructor(
    private val conversationDao: ConversationDao,
    private val messageDao: MessageDao,
    @ApplicationContext private val context: Context,
    @ApplicationScope private val applicationScope: CoroutineScope,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : ChatRepository {

    override fun observeConversations(): Flow<List<Conversation>> =
        conversationDao.observeAll(GuestSession.GUEST_ID).map { entities ->
            entities.map { it.toDomain() }
        }

    override fun observeMessages(conversationId: Long): Flow<List<Message>> =
        messageDao.observeSent(conversationId).map { entities ->
            entities.map { it.toDomain() }
        }

    override suspend fun getOrCreateConversation(product: Product): Result<Conversation> =
        withContext(ioDispatcher) {
            runCatching {
                val seller = MockSellerResolver.resolve(product)
                    ?: error("Cannot create conversation for local listing")
                val buyerId = GuestSession.GUEST_ID
                val existing = conversationDao.findByUniqueKey(
                    buyerId = buyerId,
                    sellerId = seller.sellerId,
                    productId = product.id,
                )
                if (existing != null) {
                    return@runCatching existing.toDomain()
                }
                val now = System.currentTimeMillis()
                val id = conversationDao.insert(
                    com.zoti321.c2cmarket.data.local.entity.ConversationEntity(
                        productId = product.id,
                        productTitle = product.title,
                        productImageUrl = product.imageUrl,
                        sellerId = seller.sellerId,
                        sellerDisplayName = seller.sellerDisplayName,
                        buyerId = buyerId,
                        lastMessagePreview = "",
                        lastMessageAt = now,
                        unreadCount = 0,
                        createdAt = now,
                    ),
                )
                conversationDao.getById(id)?.toDomain()
                    ?: error("Failed to load created conversation")
            }
        }

    override suspend fun saveDraft(conversationId: Long, body: String) =
        withContext(ioDispatcher) {
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
                        senderId = GuestSession.GUEST_ID,
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

                messageDao.deleteDraft(conversationId)
                val now = System.currentTimeMillis()
                val messageId = messageDao.insert(
                    MessageEntity(
                        conversationId = conversationId,
                        senderId = GuestSession.GUEST_ID,
                        body = trimmed,
                        status = MessageStatus.SENT.toEntityValue(),
                        sentAt = now,
                        isRead = true,
                    ),
                )
                conversationDao.updatePreview(
                    id = conversationId,
                    preview = trimmed,
                    lastMessageAt = now,
                    unreadCount = 0,
                )

                if (conversation.sellerId.startsWith(MOCK_SELLER_PREFIX)) {
                    scheduleMockReply(conversationId, conversation.sellerId)
                }

                MessageEntity(
                    id = messageId,
                    conversationId = conversationId,
                    senderId = GuestSession.GUEST_ID,
                    body = trimmed,
                    status = MessageStatus.SENT.toEntityValue(),
                    sentAt = now,
                    isRead = true,
                ).toDomain()
            }
        }

    override suspend fun markConversationRead(conversationId: Long) =
        withContext(ioDispatcher) {
            messageDao.markSellerMessagesRead(conversationId, GuestSession.GUEST_ID)
            conversationDao.clearUnread(conversationId)
        }

    override fun observeDraft(conversationId: Long): Flow<String> =
        messageDao.observeDraft(conversationId)

    private fun scheduleMockReply(conversationId: Long, sellerId: String) {
        applicationScope.launch(ioDispatcher) {
            delay(MOCK_SELLER_REPLY_DELAY_MS)
            val conversation = conversationDao.getById(conversationId) ?: return@launch
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
                unreadCount = conversation.unreadCount + 1,
            )
        }
    }

    companion object {
        const val MOCK_SELLER_REPLY_DELAY_MS = 3_000L
        const val MAX_MESSAGE_LENGTH = 500
        private const val MOCK_SELLER_PREFIX = "mock-seller-"
    }
}
