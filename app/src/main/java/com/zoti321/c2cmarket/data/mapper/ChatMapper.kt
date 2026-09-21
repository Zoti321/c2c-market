package com.zoti321.c2cmarket.data.mapper

import com.zoti321.c2cmarket.data.local.entity.ConversationEntity
import com.zoti321.c2cmarket.data.local.entity.MessageEntity
import com.zoti321.c2cmarket.domain.model.Conversation
import com.zoti321.c2cmarket.domain.model.Message
import com.zoti321.c2cmarket.domain.model.MessageStatus

fun ConversationEntity.toDomain(): Conversation = Conversation(
    id = id,
    productId = productId,
    productTitle = productTitle,
    productImageUrl = productImageUrl,
    sellerId = sellerId,
    sellerDisplayName = sellerDisplayName,
    buyerId = buyerId,
    buyerDisplayName = buyerDisplayName,
    lastMessagePreview = lastMessagePreview,
    lastMessageAt = lastMessageAt,
    unreadCount = unreadCount,
    sellerUnreadCount = sellerUnreadCount,
    createdAt = createdAt,
    remoteId = remoteId,
)

fun MessageEntity.toDomain(): Message = Message(
    id = id,
    conversationId = conversationId,
    senderId = senderId,
    body = body,
    status = MessageStatus.valueOf(status),
    sentAt = sentAt,
    isRead = isRead,
)

fun MessageStatus.toEntityValue(): String = name
