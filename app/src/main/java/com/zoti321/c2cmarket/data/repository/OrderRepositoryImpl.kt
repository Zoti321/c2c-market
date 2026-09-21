package com.zoti321.c2cmarket.data.repository

import com.zoti321.c2cmarket.data.error.EmptyCartException
import com.zoti321.c2cmarket.data.firebase.FirebaseAuthGateway
import com.zoti321.c2cmarket.data.local.dao.CartDao
import com.zoti321.c2cmarket.data.local.dao.OrderDao
import com.zoti321.c2cmarket.data.local.entity.OrderEntity
import com.zoti321.c2cmarket.data.local.entity.OrderLineItemEntity
import com.zoti321.c2cmarket.data.local.entity.SyncStateValues
import com.zoti321.c2cmarket.data.mapper.toDomain
import com.zoti321.c2cmarket.data.mapper.toSummary
import com.zoti321.c2cmarket.di.ApplicationScope
import com.zoti321.c2cmarket.di.IoDispatcher
import com.zoti321.c2cmarket.domain.datasource.OrderRemoteDataSource
import com.zoti321.c2cmarket.domain.datasource.OrderSyncExtras
import com.zoti321.c2cmarket.domain.model.Order
import com.zoti321.c2cmarket.domain.model.OrderStatus
import com.zoti321.c2cmarket.domain.model.OrderSummary
import com.zoti321.c2cmarket.domain.model.ShippingInfo
import com.zoti321.c2cmarket.domain.model.UserIds
import com.zoti321.c2cmarket.domain.model.displayOrderNumber
import com.zoti321.c2cmarket.domain.model.isMeetupOrder
import com.zoti321.c2cmarket.domain.repository.AuthRepository
import com.zoti321.c2cmarket.domain.repository.ListingRepository
import com.zoti321.c2cmarket.domain.repository.OrderRepository
import com.zoti321.c2cmarket.domain.scheduler.OrderNotificationKind
import com.zoti321.c2cmarket.domain.scheduler.OrderNotificationScheduler
import com.zoti321.c2cmarket.notification.NotificationHelper
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
@Singleton
class OrderRepositoryImpl @Inject constructor(
    private val orderDao: OrderDao,
    private val cartDao: CartDao,
    private val authRepository: AuthRepository,
    private val listingRepository: ListingRepository,
    private val notificationHelper: NotificationHelper,
    private val orderNotificationScheduler: OrderNotificationScheduler,
    private val orderRemote: OrderRemoteDataSource,
    private val firebaseAuthGateway: FirebaseAuthGateway,
    @ApplicationScope private val applicationScope: CoroutineScope,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : OrderRepository {

    override suspend fun placeOrder(shipping: ShippingInfo): Result<Order> = withContext(ioDispatcher) {
        runCatching {
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

            val remoteId = if (isMeetupOrder && shouldUseRemote()) UUID.randomUUID().toString() else null
            val orderEntity = OrderEntity(
                guestId = userId,
                totalAmount = total,
                status = if (isMeetupOrder) OrderStatus.PENDING.name else OrderStatus.COMPLETED.name,
                createdAt = now,
                shippingReceiverName = shipping.receiverName,
                shippingPhone = shipping.phone,
                shippingAddress = shipping.address,
                meetupLocation = meetupLocation,
                remoteId = remoteId,
                syncState = if (remoteId != null) SyncStateValues.PENDING else SyncStateValues.SYNCED,
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

            if (isMeetupOrder && remoteId != null) {
                val sellerId = localCatalogIds.firstNotNullOfOrNull { listingRepository.getSellerId(it) }
                    ?: error("Missing seller for meetup order")
                orderRemote.createOrder(order, remoteId, sellerId)
                    .onSuccess { orderDao.updateRemoteSync(orderId, remoteId, SyncStateValues.SYNCED) }
                    .onFailure { scheduleOrderRetry() }
            } else if (notificationHelper.hasNotificationPermission() && !isMeetupOrder) {
                orderNotificationScheduler.schedule(orderId, order.displayOrderNumber())
            }

            order
        }
    }

    override suspend fun resolveLocalId(remoteOrderId: String): Long? =
        withContext(ioDispatcher) {
            orderDao.findByRemoteId(remoteOrderId)?.id
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
                    isSellerOrder(orderDao, listingRepository, orderId, userId) ->
                        order.toDomain(lineItems)
                    else -> null
                }
            }
        }

    override suspend fun isSellerForOrder(orderId: Long): Boolean {
        val userId = authRepository.currentUserId().first()
        return isSellerOrder(orderDao, listingRepository, orderId, userId)
    }

    override suspend fun confirmOrderAsSeller(orderId: Long): Result<Unit> = withContext(ioDispatcher) {
        runCatching {
            requireSeller(orderDao, listingRepository, authRepository, orderId)
            val order = loadOrder(orderDao, orderId)
            require(order.isMeetupOrder()) { "Not a meetup order" }
            require(order.status == OrderStatus.PENDING) { "Invalid status" }
            orderDao.updateStatus(orderId, OrderStatus.CONFIRMED.name)
            syncRemoteStatus(order, OrderStatus.CONFIRMED)
        }
    }

    override suspend fun confirmMeetupAsBuyer(orderId: Long): Result<Unit> = withContext(ioDispatcher) {
        runCatching {
            val userId = authRepository.currentUserId().first()
            val order = loadOrder(orderDao, orderId)
            require(order.guestId == userId) { "Not buyer" }
            require(order.isMeetupOrder()) { "Not a meetup order" }
            require(order.status == OrderStatus.CONFIRMED) { "Invalid status" }
            orderDao.setBuyerMeetupConfirmed(orderId)
            syncMeetupConfirm(orderId)
            tryCompleteMeetup(
                orderDao = orderDao,
                listingRepository = listingRepository,
                orderRemote = orderRemote,
                orderId = orderId,
            )
        }
    }

    override suspend fun confirmMeetupAsSeller(orderId: Long): Result<Unit> = withContext(ioDispatcher) {
        runCatching {
            requireSeller(orderDao, listingRepository, authRepository, orderId)
            val order = loadOrder(orderDao, orderId)
            require(order.isMeetupOrder()) { "Not a meetup order" }
            require(order.status == OrderStatus.CONFIRMED) { "Invalid status" }
            orderDao.setSellerMeetupConfirmed(orderId)
            syncMeetupConfirm(orderId)
            tryCompleteMeetup(
                orderDao = orderDao,
                listingRepository = listingRepository,
                orderRemote = orderRemote,
                orderId = orderId,
            )
        }
    }

    override suspend fun cancelOrderAsSeller(orderId: Long): Result<Unit> = withContext(ioDispatcher) {
        runCatching {
            requireSeller(orderDao, listingRepository, authRepository, orderId)
            val order = loadOrder(orderDao, orderId)
            require(order.isMeetupOrder()) { "Not a meetup order" }
            require(order.status == OrderStatus.PENDING || order.status == OrderStatus.CONFIRMED) {
                "Invalid status"
            }
            orderDao.updateStatus(orderId, OrderStatus.CANCELLED.name)
            listingRepository.markAvailableForOrder(orderId)
            syncRemoteStatus(order, OrderStatus.CANCELLED)
        }
    }

    override suspend fun retryPendingUploads() = withContext(ioDispatcher) {
        if (!shouldUseRemote()) return@withContext
        orderDao.getPendingOrders().forEach { entity ->
            val remoteId = entity.remoteId ?: return@forEach
            val lineItems = orderDao.observeLineItems(entity.id).first()
            val order = entity.toDomain(lineItems)
            val sellerId = orderDao.getLocalLineItemProductIds(entity.id)
                .firstNotNullOfOrNull { listingRepository.getSellerId(it) } ?: return@forEach
            orderRemote.createOrder(order, remoteId, sellerId)
                .onSuccess { orderDao.updateRemoteSync(entity.id, remoteId, SyncStateValues.SYNCED) }
        }
    }

    private suspend fun syncRemoteStatus(order: Order, status: OrderStatus) {
        val remoteId = orderDao.getById(order.id)?.remoteId ?: return
        if (!shouldUseRemote()) return
        orderRemote.syncOrderStatus(remoteId, status)
    }

    private suspend fun syncMeetupConfirm(orderId: Long) {
        val entity = orderDao.getById(orderId) ?: return
        val remoteId = entity.remoteId ?: return
        if (!shouldUseRemote()) return
        orderRemote.syncMeetupConfirm(
            remoteId,
            entity.buyerMeetupConfirmed,
            entity.sellerMeetupConfirmed,
        )
    }

    private suspend fun shouldUseRemote(): Boolean =
        firebaseAuthGateway.isSignedIn() &&
            authRepository.currentUserId().first() != UserIds.GUEST

    private fun scheduleOrderRetry() {
        applicationScope.launch(ioDispatcher) {
            delay(RETRY_DELAY_MS)
            retryPendingUploads()
        }
    }

    companion object {
        private const val RETRY_DELAY_MS = 5_000L
    }
}

private suspend fun tryCompleteMeetup(
    orderDao: OrderDao,
    listingRepository: ListingRepository,
    orderRemote: OrderRemoteDataSource,
    orderId: Long,
) {
    val order = loadOrder(orderDao, orderId)
    val updated = orderDao.getById(orderId) ?: return
    if (!updated.buyerMeetupConfirmed || !updated.sellerMeetupConfirmed) return
    orderDao.updateStatus(orderId, OrderStatus.COMPLETED.name)
    listingRepository.markSoldForOrder(orderId)
    updated.remoteId?.let { remoteId ->
        orderRemote.syncOrderStatus(remoteId, OrderStatus.COMPLETED)
    }
}

private suspend fun loadOrder(orderDao: OrderDao, orderId: Long): Order {
    val entity = orderDao.getById(orderId) ?: error("Order not found")
    val lineItems = orderDao.observeLineItems(orderId).first()
    return entity.toDomain(lineItems)
}

private suspend fun requireSeller(
    orderDao: OrderDao,
    listingRepository: ListingRepository,
    authRepository: AuthRepository,
    orderId: Long,
) {
    val userId = authRepository.currentUserId().first()
    require(isSellerOrder(orderDao, listingRepository, orderId, userId)) { "Not seller" }
}

private suspend fun isSellerOrder(
    orderDao: OrderDao,
    listingRepository: ListingRepository,
    orderId: Long,
    userId: String,
): Boolean {
    val localProductIds = orderDao.getLocalLineItemProductIds(orderId)
    return localProductIds.any { catalogId ->
        listingRepository.getSellerId(catalogId) == userId
    }
}
