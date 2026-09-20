package com.zoti321.c2cmarket.domain.repository

import com.zoti321.c2cmarket.domain.model.Conversation
import com.zoti321.c2cmarket.domain.model.Message
import com.zoti321.c2cmarket.domain.model.Product
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    fun observeConversations(): Flow<List<Conversation>>

    fun observeMessages(conversationId: Long): Flow<List<Message>>

    suspend fun getOrCreateConversation(product: Product): Result<Conversation>

    suspend fun saveDraft(conversationId: Long, body: String)

    suspend fun sendMessage(conversationId: Long, body: String): Result<Message>

    suspend fun markConversationRead(conversationId: Long)

    fun observeDraft(conversationId: Long): Flow<String>
}
