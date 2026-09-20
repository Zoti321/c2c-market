package com.zoti321.c2cmarket.data.repository

import com.zoti321.c2cmarket.domain.model.Conversation
import com.zoti321.c2cmarket.domain.model.Message
import com.zoti321.c2cmarket.domain.model.MessageStatus
import com.zoti321.c2cmarket.domain.model.Product
import com.zoti321.c2cmarket.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeChatRepository(
    conversations: List<Conversation> = emptyList(),
    messages: Map<Long, List<Message>> = emptyMap(),
    drafts: Map<Long, String> = emptyMap(),
    var sendResult: Result<Message>? = null,
) : ChatRepository {

    private val conversationsState = MutableStateFlow(conversations)
    private val messagesState = MutableStateFlow(messages)
    private val draftsState = MutableStateFlow(drafts)

    val markReadCalls = mutableListOf<Long>()
    val sendCalls = mutableListOf<Pair<Long, String>>()

    override fun observeConversations(): Flow<List<Conversation>> = conversationsState

    override fun observeMessages(conversationId: Long): Flow<List<Message>> =
        messagesState.map { it[conversationId].orEmpty() }

    override suspend fun getOrCreateConversation(product: Product): Result<Conversation> =
        Result.failure(UnsupportedOperationException())

    override suspend fun saveDraft(conversationId: Long, body: String) {
        draftsState.value = draftsState.value + (conversationId to body)
    }

    override suspend fun sendMessage(conversationId: Long, body: String): Result<Message> {
        sendCalls += conversationId to body
        sendResult?.let { return it }
        val message = Message(
            id = 1L,
            conversationId = conversationId,
            senderId = "guest",
            body = body,
            status = MessageStatus.SENT,
            sentAt = System.currentTimeMillis(),
            isRead = true,
        )
        val existing = messagesState.value[conversationId].orEmpty()
        messagesState.value = messagesState.value + (conversationId to (existing + message))
        draftsState.value = draftsState.value + (conversationId to "")
        return Result.success(message)
    }

    override suspend fun markConversationRead(conversationId: Long) {
        markReadCalls += conversationId
    }

    override fun observeDraft(conversationId: Long): Flow<String> =
        draftsState.map { it[conversationId].orEmpty() }

    fun setConversations(conversations: List<Conversation>) {
        conversationsState.value = conversations
    }

    fun setMessages(conversationId: Long, messages: List<Message>) {
        messagesState.value = messagesState.value + (conversationId to messages)
    }
}
