package com.zoti321.c2cmarket.notification

import com.zoti321.c2cmarket.domain.repository.ChatRepository
import com.zoti321.c2cmarket.domain.repository.OrderRepository
import com.zoti321.c2cmarket.domain.scheduler.OrderNotificationKind
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first

@Singleton
class FcmNotificationDispatcher @Inject constructor(
    private val chatRepository: ChatRepository,
    private val orderRepository: OrderRepository,
    private val pushPresenter: PushNotificationPresenter,
    private val foregroundState: AppForegroundState,
) {
    suspend fun dispatch(data: Map<String, String>) {
        when (data["type"]) {
            "chat" -> dispatchChat(data)
            "order" -> dispatchOrder(data)
        }
    }

    private suspend fun dispatchChat(data: Map<String, String>) {
        val push = data.toChatPush()
        if (push != null && !foregroundState.isForeground.first()) {
            val localId = chatRepository.resolveLocalConversationId(push.remoteConversationId)
            if (localId != null) {
                pushPresenter.showChatMessage(localId, push.title, push.body)
            } else {
                pushPresenter.showChatMessageFallback(push.title, push.body)
            }
        }
    }

    private suspend fun dispatchOrder(data: Map<String, String>) {
        val push = data.toOrderPush() ?: return
        val localId = orderRepository.resolveLocalId(push.remoteOrderId)
        if (localId != null) {
            pushPresenter.showOrderUpdate(localId, push.title, push.body, push.kind)
        } else {
            pushPresenter.showOrderUpdateFallback(push.title, push.body)
        }
    }
}

private data class ChatPush(
    val remoteConversationId: String,
    val title: String,
    val body: String,
)

private data class OrderPush(
    val remoteOrderId: String,
    val title: String,
    val body: String,
    val kind: OrderNotificationKind,
)

private fun Map<String, String>.toChatPush(): ChatPush? {
    val remoteConversationId = this["remoteConversationId"]
    val title = this["title"]
    val body = this["body"]
    return if (remoteConversationId != null && title != null && body != null) {
        ChatPush(remoteConversationId, title, body)
    } else {
        null
    }
}

private fun Map<String, String>.toOrderPush(): OrderPush? {
    val remoteOrderId = this["remoteOrderId"]
    val title = this["title"]
    val body = this["body"]
    val kind = this["orderKind"]?.let { name ->
        runCatching { OrderNotificationKind.valueOf(name) }.getOrNull()
    } ?: return null
    return if (remoteOrderId != null && title != null && body != null) {
        OrderPush(remoteOrderId, title, body, kind)
    } else {
        null
    }
}
