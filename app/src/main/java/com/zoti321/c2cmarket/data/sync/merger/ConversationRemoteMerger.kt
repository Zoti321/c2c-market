package com.zoti321.c2cmarket.data.sync.merger

import com.zoti321.c2cmarket.data.local.dao.ConversationDao
import com.zoti321.c2cmarket.data.local.entity.ConversationEntity
import com.zoti321.c2cmarket.domain.datasource.RemoteConversation
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConversationRemoteMerger @Inject constructor(
    private val conversationDao: ConversationDao,
) {
    suspend fun mergeAll(remote: List<RemoteConversation>) {
        remote.forEach { item ->
            val existing = conversationDao.findByRemoteId(item.remoteId)
                ?: conversationDao.findByUniqueKey(item.buyerId, item.sellerId, item.productId)
            if (existing == null) {
                conversationDao.insert(
                    ConversationEntity(
                        productId = item.productId,
                        productTitle = item.productTitle,
                        productImageUrl = item.productImageUrl,
                        sellerId = item.sellerId,
                        sellerDisplayName = item.sellerDisplayName,
                        buyerId = item.buyerId,
                        buyerDisplayName = item.buyerDisplayName,
                        lastMessagePreview = item.lastMessagePreview,
                        lastMessageAt = item.lastMessageAt,
                        unreadCount = item.unreadCount,
                        sellerUnreadCount = item.sellerUnreadCount,
                        createdAt = item.createdAt,
                        remoteId = item.remoteId,
                    ),
                )
            } else {
                conversationDao.update(
                    existing.copy(
                        remoteId = item.remoteId,
                        productTitle = item.productTitle,
                        productImageUrl = item.productImageUrl,
                        buyerDisplayName = item.buyerDisplayName,
                        sellerDisplayName = item.sellerDisplayName,
                    ),
                )
                conversationDao.mergePreviewIfNewer(
                    id = existing.id,
                    preview = item.lastMessagePreview,
                    lastMessageAt = item.lastMessageAt,
                    unreadCount = item.unreadCount,
                    sellerUnreadCount = item.sellerUnreadCount,
                )
            }
        }
    }
}
