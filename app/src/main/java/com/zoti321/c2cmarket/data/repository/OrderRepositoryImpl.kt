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
import com.zoti321.c2cmarket.domain.model.isMeetupOrder
import com.zoti321.c2cmarket.domain.repository.AuthRepository
import com.zoti321.c2cmarket.domain.repository.ListingRepository
import com.zoti321.c2cmarket.domain.repository.OrderRepository
import com.zoti321.c2cmarket.domain.scheduler.OrderNotificationKind
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
    private val listingRepository: ListingRepository,
    private val notificationHelper: NotificationHelper,
    private val orderNotificationScheduler: OrderNotificationScheduler,
) : OrderRepository {

    override suspend fun placeOrder(shipping: ShippingInfo): Result<Order> = runCatching {
        val userId = authRepository.currentUserId().first()
        val cartItems = cartDao.observeAll(userId).first()
        if (cartItems.isEmpty()) throw EmptyCartException()

        val total = cartItems.sumOf { it.unitPrice * it.quantity }
        val now = System.currentTimeMillis()
        val localCatalogIds = cartItems.map { it.productId }.filter { it < 0 }.distinct()
        val isMeetupOrder = localCatalogIds.isNotEmpty()
        val meetupLocation = if (isMeetupOrder) {
            localCatalogIds.firstNotNullOfOrNull { catalogId ->
                listingRepository.getProductByCatalogId(catalogId).getOrNull()
                    ?.meetupLocation?.takeIf { it.isNotBlank() }
            }
        } else {
            null
        }

        val orderEntity = OrderEntity(
            guestId = userId,
            totalAmount = total,
            status = if (isMeetupOrder) OrderStatus.PENDING.name else OrderStatus.COMPLETED.name,
            createdAt = now,
            shippingReceiverName = shipping.receiverName,
            shippingPhone = shipping.phone,
            shippingAddress = shipping.address,
            meetupLocation = meetupLocation,
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

        val orderId = orderDao.placeOrderWithClearCart(orderEntity, lineEntities, userId)
        listingRepository.markReservedForCheckout(localCatalogIds)
        val lineItems = lineEntities.map { it.copy(orderId = orderId) }
        val order = orderEntity.copy(id = orderId).toDomain(lineItems)

        if (notificationHelper.hasNotificationPermission()) {
            if (isMeetupOrder) {
                orderNotificationScheduler.schedule(
                    orderId,
                    order.displayOrderNumber(),
                    OrderNotificationKind.PENDING_SELLER,
                )
            } else {
                orderNotificationScheduler.schedule(orderId, order.displayOrderNumber())
            }
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

    override fun observeOrdersAsSeller(): Flow<List<OrderSummary>> =
        authRepository.currentUserId().flatMapLatest { sellerId ->
            combine(
                orderDao.observeOrdersAsSeller(sellerId),
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
                    order.guestId == userId -> order.toDomain(lineItems)
                    isSellerOrder(orderId, userId) -> order.toDomain(lineItems)
                    else -> null
                }
            }
        }

    override suspend fun isSellerForOrder(orderId: Long): Boolean {
        val userId = authRepository.currentUserId().first()
        return isSellerOrder(orderId, userId)
    }

    override suspend fun confirmOrderAsSeller(orderId: Long): Result<Unit> = runCatching {
        requireSeller(orderId)
        val order = loadOrder(orderId)
        require(order.isMeetupOrder()) { "Not a meetup order" }
        require(order.status == OrderStatus.PENDING) { "Invalid status" }
        orderDao.updateStatus(orderId, OrderStatus.CONFIRMED.name)
        if (notificationHelper.hasNotificationPermission()) {
            orderNotificationScheduler.schedule(
                orderId,
                order.displayOrderNumber(),
                OrderNotificationKind.CONFIRMED_BUYER,
            )
        }
    }

    override suspend fun confirmMeetupAsBuyer(orderId: Long): Result<Unit> = runCatching {
        val userId = authRepository.currentUserId().first()
        val order = loadOrder(orderId)
        require(order.guestId == userId) { "Not buyer" }
        require(order.isMeetupOrder()) { "Not a meetup order" }
        require(order.status == OrderStatus.CONFIRMED) { "Invalid status" }
        orderDao.setBuyerMeetupConfirmed(orderId)
        tryCompleteMeetup(orderId)
    }

    override suspend fun confirmMeetupAsSeller(orderId: Long): Result<Unit> = runCatching {
        requireSeller(orderId)
        val order = loadOrder(orderId)
        require(order.isMeetupOrder()) { "Not a meetup order" }
        require(order.status == OrderStatus.CONFIRMED) { "Invalid status" }
        orderDao.setSellerMeetupConfirmed(orderId)
        tryCompleteMeetup(orderId)
    }

    override suspend fun cancelOrderAsSeller(orderId: Long): Result<Unit> = runCatching {
        requireSeller(orderId)
        val order = loadOrder(orderId)
        require(order.isMeetupOrder()) { "Not a meetup order" }
        require(order.status == OrderStatus.PENDING || order.status == OrderStatus.CONFIRMED) {
            "Invalid status"
        }
        orderDao.updateStatus(orderId, OrderStatus.CANCELLED.name)
        listingRepository.markAvailableForOrder(orderId)
    }

    private suspend fun tryCompleteMeetup(orderId: Long) {
        val order = loadOrder(orderId)
        val updated = orderDao.getById(orderId) ?: return
        if (!updated.buyerMeetupConfirmed || !updated.sellerMeetupConfirmed) return
        orderDao.updateStatus(orderId, OrderStatus.COMPLETED.name)
        listingRepository.markSoldForOrder(orderId)
        if (notificationHelper.hasNotificationPermission()) {
            orderNotificationScheduler.schedule(
                orderId,
                order.displayOrderNumber(),
                OrderNotificationKind.COMPLETED,
            )
        }
    }

    private suspend fun loadOrder(orderId: Long): Order {
        val entity = orderDao.getById(orderId) ?: error("Order not found")
        val lineItems = orderDao.observeLineItems(orderId).first()
        return entity.toDomain(lineItems)
    }

    private suspend fun requireSeller(orderId: Long) {
        require(isSellerForOrder(orderId)) { "Not seller" }
    }

    private suspend fun isSellerOrder(orderId: Long, userId: String): Boolean {
        val localProductIds = orderDao.getLocalLineItemProductIds(orderId)
        return localProductIds.any { catalogId ->
            listingRepository.getSellerId(catalogId) == userId
        }
    }
}
