package com.zoti321.c2cmarket.data.repository

import com.zoti321.c2cmarket.data.error.EmptyCartException
import com.zoti321.c2cmarket.data.local.dao.CartDao
import com.zoti321.c2cmarket.data.local.dao.OrderDao
import com.zoti321.c2cmarket.data.local.entity.OrderEntity
import com.zoti321.c2cmarket.data.local.entity.OrderLineItemEntity
import com.zoti321.c2cmarket.data.mapper.toDomain
import com.zoti321.c2cmarket.data.mapper.toSummary
import com.zoti321.c2cmarket.domain.model.Order
import com.zoti321.c2cmarket.domain.model.OrderStatus
import com.zoti321.c2cmarket.domain.model.OrderSummary
import com.zoti321.c2cmarket.domain.model.ShippingInfo
import com.zoti321.c2cmarket.domain.model.displayOrderNumber
import com.zoti321.c2cmarket.domain.repository.AuthRepository
import com.zoti321.c2cmarket.domain.repository.OrderRepository
import com.zoti321.c2cmarket.domain.scheduler.OrderNotificationScheduler
import com.zoti321.c2cmarket.notification.NotificationHelper
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest

@Singleton
class OrderRepositoryImpl @Inject constructor(
    private val orderDao: OrderDao,
    private val cartDao: CartDao,
    private val authRepository: AuthRepository,
    private val notificationHelper: NotificationHelper,
    private val orderNotificationScheduler: OrderNotificationScheduler,
) : OrderRepository {

    override suspend fun placeOrder(shipping: ShippingInfo): Result<Order> = runCatching {
        val cartItems = cartDao.observeAll().first()
        if (cartItems.isEmpty()) throw EmptyCartException()

        val total = cartItems.sumOf { it.unitPrice * it.quantity }
        val now = System.currentTimeMillis()
        val userId = authRepository.currentUserId().first()

        val orderEntity = OrderEntity(
            guestId = userId,
            totalAmount = total,
            status = OrderStatus.COMPLETED.name,
            createdAt = now,
            shippingReceiverName = shipping.receiverName,
            shippingPhone = shipping.phone,
            shippingAddress = shipping.address,
        )

        val lineEntities = cartItems.map { cart ->
            OrderLineItemEntity(
                orderId = 0,
                productId = cart.productId,
                title = cart.title,
                unitPrice = cart.unitPrice,
                quantity = cart.quantity,
                imageUrl = cart.imageUrl,
            )
        }

        val orderId = orderDao.placeOrderWithClearCart(orderEntity, lineEntities)
        val lineItems = lineEntities.map { it.copy(orderId = orderId) }
        val order = orderEntity.copy(id = orderId).toDomain(lineItems)

        if (notificationHelper.hasNotificationPermission()) {
            orderNotificationScheduler.schedule(orderId, order.displayOrderNumber())
        }

        order
    }

    override fun observeOrders(): Flow<List<OrderSummary>> =
        authRepository.currentUserId().flatMapLatest { userId ->
            combine(
                orderDao.observeOrdersByUserId(userId),
                orderDao.observeAllLineItems(),
            ) { orders, allLineItems ->
                orders.map { order ->
                    val itemCount = allLineItems
                        .filter { it.orderId == order.id }
                        .sumOf { it.quantity }
                    order.toSummary(itemCount)
                }
            }
        }

    override fun observeOrder(orderId: Long): Flow<Order?> =
        authRepository.currentUserId().flatMapLatest { userId ->
            combine(
                orderDao.observeOrder(orderId),
                orderDao.observeLineItems(orderId),
            ) { order, lineItems ->
                when {
                    order == null -> null
                    order.guestId != userId -> null
                    else -> order.toDomain(lineItems)
                }
            }
        }
}
