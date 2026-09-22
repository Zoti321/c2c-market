package com.zoti321.c2cmarket.data.sync.merger

import com.zoti321.c2cmarket.data.local.dao.ConversationDao
import com.zoti321.c2cmarket.data.local.dao.MessageDao
import com.zoti321.c2cmarket.data.local.entity.MessageEntity
import com.zoti321.c2cmarket.data.local.entity.SyncStateValues
import com.zoti321.c2cmarket.data.mapper.toEntityValue
import com.zoti321.c2cmarket.domain.datasource.RemoteMessage
import com.zoti321.c2cmarket.domain.model.MessageStatus
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MessageRemoteMerger @Inject constructor(
    private val conversationDao: ConversationDao,
    private val messageDao: MessageDao,
) {
    suspend fun merge(conversationRemoteId: String, remote: RemoteMessage) {
        if (messageDao.findByRemoteId(remote.remoteId) != null) return
        val conversation = conversationDao.findByRemoteId(conversationRemoteId) ?: return
        messageDao.insert(
            MessageEntity(
                conversationId = conversation.id,
                senderId = remote.senderId,
                body = remote.body,
                status = MessageStatus.SENT.toEntityValue(),
                sentAt = remote.sentAt,
                isRead = remote.isRead,
                remoteId = remote.remoteId,
                syncState = SyncStateValues.SYNCED,
            ),
        )
        conversationDao.mergePreviewIfNewer(
            id = conversation.id,
            preview = remote.body,
            lastMessageAt = remote.sentAt,
            unreadCount = conversation.unreadCount,
            sellerUnreadCount = conversation.sellerUnreadCount,
        )
    }
}
