package com.zoti321.c2cmarket.domain.datasource

import com.zoti321.c2cmarket.domain.model.Conversation
import com.zoti321.c2cmarket.domain.model.Message
import kotlinx.coroutines.flow.Flow

interface ChatRemoteDataSource {
    suspend fun ensureConversation(conversation: Conversation, remoteId: String): Result<Unit>

    suspend fun uploadMessage(conversationRemoteId: String, message: Message, remoteId: String): Result<Unit>

    fun observeRemoteConversations(userId: String): Flow<List<RemoteConversation>>

    fun observeRemoteMessages(conversationRemoteId: String): Flow<List<RemoteMessage>>

    suspend fun retryPendingUploads()
}
