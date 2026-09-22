package com.zoti321.c2cmarket.data.sync

interface RemoteSyncGateway {
    fun start()

    fun observeActiveConversationMessages(conversationRemoteId: String)

    fun stopActiveConversationMessages()

    suspend fun unregisterFcmToken()
}
