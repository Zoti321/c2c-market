package com.zoti321.c2cmarket.notification

import com.zoti321.c2cmarket.domain.model.Conversation
import com.zoti321.c2cmarket.domain.model.Message
import com.zoti321.c2cmarket.domain.model.MessageStatus
import com.zoti321.c2cmarket.domain.model.Order
import com.zoti321.c2cmarket.domain.model.OrderSummary
import com.zoti321.c2cmarket.domain.model.Product
import com.zoti321.c2cmarket.domain.model.ShippingInfo
import com.zoti321.c2cmarket.domain.scheduler.OrderNotificationKind
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FcmNotificationDispatcherTest {

    private lateinit var chatRepo: RecordingChatRepository
    private lateinit var orderRepo: RecordingOrderRepository
    private lateinit var pushPresenter: RecordingPushPresenter
    private lateinit var foregroundState: FakeForegroundState
    private lateinit var dispatcher: FcmNotificationDispatcher

    @Before
    fun setUp() {
        chatRepo = RecordingChatRepository()
        orderRepo = RecordingOrderRepository()
        pushPresenter = RecordingPushPresenter()
        foregroundState = FakeForegroundState()
        dispatcher = FcmNotificationDispatcher(
            chatRepository = chatRepo,
            orderRepository = orderRepo,
            pushPresenter = pushPresenter,
            foregroundState = foregroundState,
        )
    }

    @Test
    fun dispatchChat_resolvesRemoteIdToLocalDeepLink() = runTest {
        chatRepo.localIdForRemote = 42L

        dispatcher.dispatch(
            mapOf(
                "type" to "chat",
                "remoteConversationId" to "buyer_seller_1",
                "title" to "新消息",
                "body" to "你好",
            ),
        )

        assertEquals(listOf(42L to "新消息"), pushPresenter.chatNotifications)
    }

    @Test
    fun dispatchChat_skipsWhenAppInForeground() = runTest {
        foregroundState.isForeground.value = true
        chatRepo.localIdForRemote = 42L

        dispatcher.dispatch(
            mapOf(
                "type" to "chat",
                "remoteConversationId" to "buyer_seller_1",
                "title" to "新消息",
                "body" to "你好",
            ),
        )

        assertTrue(pushPresenter.chatNotifications.isEmpty())
    }

    @Test
    fun dispatchChat_fallsBackWhenLocalIdMissing() = runTest {
        dispatcher.dispatch(
            mapOf(
                "type" to "chat",
                "remoteConversationId" to "missing",
                "title" to "新消息",
                "body" to "你好",
            ),
        )

        assertEquals(listOf("新消息"), pushPresenter.chatFallbacks)
    }

    @Test
    fun dispatchOrder_resolvesRemoteIdToLocalDeepLink() = runTest {
        orderRepo.localIdForRemote = 7L

        dispatcher.dispatch(
            mapOf(
                "type" to "order",
                "remoteOrderId" to "remote-order-uuid",
                "orderKind" to "CONFIRMED_BUYER",
                "title" to "订单更新",
                "body" to "已确认",
            ),
        )

        assertEquals(
            listOf(Triple(7L, "订单更新", OrderNotificationKind.CONFIRMED_BUYER)),
            pushPresenter.orderNotifications,
        )
    }

    private class FakeForegroundState : AppForegroundState {
        override val isForeground = MutableStateFlow(false)
    }

    private class RecordingPushPresenter : PushNotificationPresenter {
        val chatNotifications = mutableListOf<Pair<Long, String>>()
        val chatFallbacks = mutableListOf<String>()
        val orderNotifications = mutableListOf<Triple<Long, String, OrderNotificationKind>>()

        override fun showChatMessage(localConversationId: Long, title: String, body: String) {
            chatNotifications += localConversationId to title
        }

        override fun showChatMessageFallback(title: String, body: String) {
            chatFallbacks += title
        }

        override fun showOrderUpdate(
            orderId: Long,
            title: String,
            body: String,
            kind: OrderNotificationKind,
        ) {
            orderNotifications += Triple(orderId, title, kind)
        }

        override fun showOrderUpdateFallback(title: String, body: String) = Unit
    }

    private class RecordingChatRepository : com.zoti321.c2cmarket.domain.repository.ChatRepository {
        var localIdForRemote: Long? = null

        override fun observeConversationsAsBuyer(): Flow<List<Conversation>> = flowOf(emptyList())
        override fun observeConversationsAsSeller(): Flow<List<Conversation>> = flowOf(emptyList())
        override fun observeConversationForCurrentUser(conversationId: Long): Flow<Conversation?> = flowOf(null)
        override fun observeMessages(conversationId: Long): Flow<List<Message>> = flowOf(emptyList())
        override suspend fun getOrCreateConversation(product: Product): Result<Conversation> =
            Result.failure(IllegalStateException())
        override suspend fun getConversationForUser(conversationId: Long): Conversation? = null
        override suspend fun saveDraft(conversationId: Long, body: String) = Unit
        override suspend fun sendMessage(conversationId: Long, body: String): Result<Message> =
            Result.success(Message(1, conversationId, "u", "b", MessageStatus.SENT, 1L, true))
        override suspend fun markConversationRead(conversationId: Long) = Unit
        override fun observeDraft(conversationId: Long): Flow<String> = flowOf("")
        override suspend fun resolveLocalConversationId(remoteId: String) = localIdForRemote
        override fun startActiveConversationSync(conversationId: Long) = Unit
        override fun stopActiveConversationSync() = Unit
        override suspend fun retryPendingUploads() = Unit
    }

    private class RecordingOrderRepository : com.zoti321.c2cmarket.domain.repository.OrderRepository {
        var localIdForRemote: Long? = null

        override suspend fun placeOrder(shipping: ShippingInfo): Result<Order> =
            Result.failure(IllegalStateException())
        override fun observeOrders(): Flow<List<OrderSummary>> = flowOf(emptyList())
        override fun observeOrdersAsSeller(): Flow<List<OrderSummary>> = flowOf(emptyList())
        override fun observeOrder(orderId: Long): Flow<Order?> = flowOf(null)
        override suspend fun isSellerForOrder(orderId: Long) = false
        override suspend fun confirmOrderAsSeller(orderId: Long) = Result.success(Unit)
        override suspend fun confirmMeetupAsBuyer(orderId: Long) = Result.success(Unit)
        override suspend fun confirmMeetupAsSeller(orderId: Long) = Result.success(Unit)
        override suspend fun cancelOrderAsSeller(orderId: Long) = Result.success(Unit)
        override suspend fun resolveLocalId(remoteOrderId: String) = localIdForRemote
        override suspend fun retryPendingUploads() = Unit
    }
}
