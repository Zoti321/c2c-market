package com.zoti321.c2cmarket.data.remote

import com.zoti321.c2cmarket.domain.datasource.ChatRemoteDataSource
import com.zoti321.c2cmarket.domain.datasource.RemoteConversation
import com.zoti321.c2cmarket.domain.datasource.RemoteMessage
import com.zoti321.c2cmarket.domain.model.Conversation
import com.zoti321.c2cmarket.domain.model.Message
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

@Singleton
class NoOpChatRemoteDataSource @Inject constructor() : ChatRemoteDataSource {
    override suspend fun ensureConversation(conversation: Conversation, remoteId: String): Result<Unit> =
        Result.success(Unit)

    override suspend fun uploadMessage(
        conversationRemoteId: String,
        message: Message,
        remoteId: String,
    ): Result<Unit> = Result.success(Unit)

    override fun observeRemoteConversations(userId: String): Flow<List<RemoteConversation>> =
        flowOf(emptyList())

    override fun observeRemoteMessages(conversationRemoteId: String): Flow<List<RemoteMessage>> =
        flowOf(emptyList())

    override suspend fun retryPendingUploads() = Unit
}
