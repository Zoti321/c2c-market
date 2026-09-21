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
        if (foregroundState.isForeground.first()) return
        val remoteConversationId = data["remoteConversationId"] ?: return
        val title = data["title"] ?: return
        val body = data["body"] ?: return
        val localId = chatRepository.resolveLocalConversationId(remoteConversationId)
        if (localId != null) {
            pushPresenter.showChatMessage(localId, title, body)
        } else {
            pushPresenter.showChatMessageFallback(title, body)
        }
    }

    private suspend fun dispatchOrder(data: Map<String, String>) {
        val remoteOrderId = data["remoteOrderId"] ?: return
        val title = data["title"] ?: return
        val body = data["body"] ?: return
        val kindName = data["orderKind"] ?: return
        val kind = runCatching { OrderNotificationKind.valueOf(kindName) }.getOrNull() ?: return
        val localId = orderRepository.resolveLocalId(remoteOrderId)
        if (localId != null) {
            pushPresenter.showOrderUpdate(localId, title, body, kind)
        } else {
            pushPresenter.showOrderUpdateFallback(title, body)
        }
    }
}
