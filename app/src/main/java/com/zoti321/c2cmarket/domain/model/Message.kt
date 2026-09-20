package com.zoti321.c2cmarket.domain.model

data class Message(
    val id: Long,
    val conversationId: Long,
    val senderId: String,
    val body: String,
    val status: MessageStatus,
    val sentAt: Long?,
    val isRead: Boolean,
)
