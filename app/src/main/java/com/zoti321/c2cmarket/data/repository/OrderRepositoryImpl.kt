package com.zoti321.c2cmarket.data.repository

import com.zoti321.c2cmarket.data.error.EmptyCartException
import com.zoti321.c2cmarket.data.local.dao.CartDao
import com.zoti321.c2cmarket.data.local.dao.OrderDao
import com.zoti321.c2cmarket.data.local.entity.OrderEntity
import com.zoti321.c2cmarket.data.local.entity.OrderLineItemEntity
import com.zoti321.c2cmarket.data.mapper.toDomain
import com.zoti321.c2cmarket.data.mapper.toSummary
import com.zoti321.c2cmarket.domain.GuestSession
import com.zoti321.c2cmarket.domain.model.Order
import com.zoti321.c2cmarket.domain.model.OrderStatus
import com.zoti321.c2cmarket.domain.model.OrderSummary
import com.zoti321.c2cmarket.domain.repository.OrderRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first

@Singleton
class OrderRepositoryImpl @Inject constructor(
    private val orderDao: OrderDao,
    private val cartDao: CartDao,
) : OrderRepository {

    override suspend fun placeOrder(): Result<Order> = runCatching {
        val cartItems = cartDao.observeAll().first()
        if (cartItems.isEmpty()) throw EmptyCartException()

        val total = cartItems.sumOf { it.unitPrice * it.quantity }
        val now = System.currentTimeMillis()

        val orderEntity = OrderEntity(
            guestId = GuestSession.GUEST_ID,
            totalAmount = total,
            status = OrderStatus.COMPLETED.name,
            createdAt = now,
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
        orderEntity.copy(id = orderId).toDomain(lineItems)
    }

    override fun observeOrders(): Flow<List<OrderSummary>> =
        combine(
            orderDao.observeAllOrders(),
            orderDao.observeAllLineItems(),
        ) { orders, allLineItems ->
            orders.map { order ->
                val itemCount = allLineItems
                    .filter { it.orderId == order.id }
                    .sumOf { it.quantity }
                order.toSummary(itemCount)
            }
        }

    override fun observeOrder(orderId: Long): Flow<Order?> =
        combine(
            orderDao.observeOrder(orderId),
            orderDao.observeLineItems(orderId),
        ) { order, lineItems ->
            order?.toDomain(lineItems)
        }
}
