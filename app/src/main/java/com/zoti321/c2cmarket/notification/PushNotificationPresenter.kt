package com.zoti321.c2cmarket.notification

import com.zoti321.c2cmarket.domain.scheduler.OrderNotificationKind

interface PushNotificationPresenter {
    fun showChatMessage(localConversationId: Long, title: String, body: String)

    fun showChatMessageFallback(title: String, body: String)

    fun showOrderUpdate(orderId: Long, title: String, body: String, kind: OrderNotificationKind)

    fun showOrderUpdateFallback(title: String, body: String)
}
