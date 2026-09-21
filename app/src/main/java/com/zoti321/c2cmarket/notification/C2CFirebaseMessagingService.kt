package com.zoti321.c2cmarket.notification

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.zoti321.c2cmarket.data.firebase.FcmTokenRepository
import com.zoti321.c2cmarket.domain.repository.ChatRepository
import com.zoti321.c2cmarket.domain.repository.OrderRepository
import com.zoti321.c2cmarket.domain.scheduler.OrderNotificationKind
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

@AndroidEntryPoint
class C2CFirebaseMessagingService : FirebaseMessagingService() {

    @Inject lateinit var notificationHelper: NotificationHelper

    @Inject lateinit var chatRepository: ChatRepository

    @Inject lateinit var orderRepository: OrderRepository

    @Inject lateinit var fcmTokenRepository: FcmTokenRepository

    @Inject lateinit var foregroundTracker: AppForegroundTracker

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        serviceScope.launch {
            fcmTokenRepository.upsertToken(token)
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val data = message.data
        if (data.isEmpty()) return
        when (data["type"]) {
            "chat" -> handleChatMessage(data)
            "order" -> handleOrderMessage(data)
        }
    }

    private fun handleChatMessage(data: Map<String, String>) {
        if (runBlocking { foregroundTracker.isForeground.first() }) return
        val remoteConversationId = data["remoteConversationId"] ?: return
        val title = data["title"] ?: return
        val body = data["body"] ?: return
        val localId = runBlocking {
            chatRepository.resolveLocalConversationId(remoteConversationId)
        }
        if (localId != null) {
            notificationHelper.showChatMessage(localId, title, body)
        } else {
            notificationHelper.showChatMessageFallback(title, body)
        }
    }

    private fun handleOrderMessage(data: Map<String, String>) {
        val remoteOrderId = data["remoteOrderId"] ?: return
        val title = data["title"] ?: return
        val body = data["body"] ?: return
        val kindName = data["orderKind"] ?: return
        val kind = runCatching { OrderNotificationKind.valueOf(kindName) }.getOrNull() ?: return
        val localId = runBlocking { orderRepository.resolveLocalId(remoteOrderId) }
        if (localId != null) {
            notificationHelper.showOrderUpdate(localId, title, body, kind)
        } else {
            notificationHelper.showOrderUpdateFallback(title, body)
        }
    }
}
