package com.zoti321.c2cmarket.data.repository

import com.zoti321.c2cmarket.data.error.EmptyCartException
import com.zoti321.c2cmarket.data.local.InMemoryDatabaseFactory
import com.zoti321.c2cmarket.data.local.dao.CartDao
import com.zoti321.c2cmarket.data.local.dao.OrderDao
import com.zoti321.c2cmarket.data.local.entity.CartItemEntity
import com.zoti321.c2cmarket.data.local.entity.ListingEntity
import com.zoti321.c2cmarket.data.local.entity.OrderEntity
import com.zoti321.c2cmarket.data.local.entity.OrderLineItemEntity
import com.zoti321.c2cmarket.domain.model.ListingStatus
import com.zoti321.c2cmarket.domain.model.OrderStatus
import com.zoti321.c2cmarket.domain.model.ShippingInfo
import com.zoti321.c2cmarket.domain.scheduler.OrderNotificationKind
import com.zoti321.c2cmarket.domain.scheduler.OrderNotificationScheduler
import com.zoti321.c2cmarket.notification.NotificationHelper
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class OrderRepositoryImplTest {

    private lateinit var cartDao: FakeCartDaoForOrder
    private lateinit var orderDao: FakeOrderDaoForOrder
    private lateinit var scheduler: RecordingOrderNotificationScheduler
    private lateinit var listingRepository: ListingRepositoryImpl
    private lateinit var repository: OrderRepositoryImpl
    private lateinit var database: com.zoti321.c2cmarket.data.local.C2CDatabase

    private val shipping = ShippingInfo(
        receiverName = "张三",
        phone = "13800138000",
        address = "广东省深圳市南山区 科技园",
    )

    @Before
    fun setUp() {
        database = InMemoryDatabaseFactory.create()
        cartDao = FakeCartDaoForOrder(
            mutableListOf(
                CartItemEntity(
                    userId = "guest",
                    productId = 1,
                    title = "Phone",
                    unitPrice = 10.0,
                    imageUrl = "https://example.com/a.png",
                    quantity = 2,
                    addedAt = 1L,
                ),
            ),
        )
        orderDao = FakeOrderDaoForOrder(cartDao)
        scheduler = RecordingOrderNotificationScheduler()
        listingRepository = ListingRepositoryImpl(
            database = database,
            listingDao = database.listingDao(),
            cartDao = database.cartDao(),
            favoriteDao = database.favoriteDao(),
            orderDao = database.orderDao(),
            authRepository = FakeAuthRepository(),
        )
        repository = OrderRepositoryImpl(
            orderDao = orderDao,
            cartDao = cartDao,
            authRepository = FakeAuthRepository(),
            listingRepository = listingRepository,
            notificationHelper = NotificationHelper(ApplicationProvider.getApplicationContext()),
            orderNotificationScheduler = scheduler,
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun placeOrder_withCartItems_clearsCartAndPersistsShipping() = runTest {
        val result = repository.placeOrder(shipping)

        assertTrue(result.isSuccess)
        val order = result.getOrThrow()
        assertEquals(20.0, order.totalAmount, 0.001)
        assertEquals(shipping.receiverName, order.shipping?.receiverName)
        assertTrue(cartDao.items.isEmpty())
        assertEquals(1, orderDao.placedLineItems.size)
        assertEquals(listOf(OrderNotificationKind.SHIPPED), scheduler.scheduledKinds)
    }

    @Test
    fun placeOrder_emptyCart_fails() = runTest {
        cartDao.clearForUser("guest")

        val result = repository.placeOrder(shipping)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is EmptyCartException)
        assertTrue(scheduler.scheduledKinds.isEmpty())
    }

    @Test
    fun placeOrder_withLocalListing_marksListingReserved() = runTest {
        val sellerId = "google:seller-1"
        database.listingDao().insert(
            ListingEntity(
                catalogId = -1,
                title = "Local Item",
                price = 50.0,
                description = "Desc",
                category = "electronics",
                imageUri = "content://img",
                sellerId = sellerId,
                createdAt = 1L,
                updatedAt = 1L,
            ),
        )
        cartDao = FakeCartDaoForOrder(
            mutableListOf(
                CartItemEntity(
                    userId = "guest",
                    productId = -1,
                    title = "Local Item",
                    unitPrice = 50.0,
                    imageUrl = "content://img",
                    quantity = 1,
                    addedAt = 1L,
                ),
            ),
        )
        orderDao = FakeOrderDaoForOrder(cartDao)
        listingRepository = ListingRepositoryImpl(
            database = database,
            listingDao = database.listingDao(),
            cartDao = database.cartDao(),
            favoriteDao = database.favoriteDao(),
            orderDao = database.orderDao(),
            authRepository = FakeAuthRepository(),
        )
        repository = OrderRepositoryImpl(
            orderDao = orderDao,
            cartDao = cartDao,
            authRepository = FakeAuthRepository(),
            listingRepository = listingRepository,
            notificationHelper = NotificationHelper(ApplicationProvider.getApplicationContext()),
            orderNotificationScheduler = scheduler,
        )

        repository.placeOrder(shipping).getOrThrow()

        val listing = database.listingDao().getByCatalogId(-1)
        assertEquals(ListingStatus.RESERVED.name, listing?.status)
        assertEquals(listOf(OrderNotificationKind.PENDING_SELLER), scheduler.scheduledKinds)
    }

    @Test
    fun observeOrdersAsSeller_returnsOrdersWithSellerListings() = runTest {
        val sellerId = "google:seller-1"
        database.listingDao().insert(
            ListingEntity(
                catalogId = -1,
                title = "Local Item",
                price = 50.0,
                description = "Desc",
                category = "electronics",
                imageUri = "content://img",
                sellerId = sellerId,
                createdAt = 1L,
                updatedAt = 1L,
            ),
        )
        val orderId = database.orderDao().insertOrder(
            OrderEntity(
                guestId = "guest",
                totalAmount = 50.0,
                status = OrderStatus.COMPLETED.name,
                createdAt = 100L,
            ),
        )
        database.orderDao().insertLineItems(
            listOf(
                OrderLineItemEntity(
                    orderId = orderId,
                    productId = -1,
                    title = "Local Item",
                    unitPrice = 50.0,
                    quantity = 1,
                    imageUrl = "content://img",
                ),
            ),
        )
        val sellerRepo = OrderRepositoryImpl(
            orderDao = database.orderDao(),
            cartDao = cartDao,
            authRepository = FakeAuthRepository(initialUserId = sellerId),
            listingRepository = listingRepository,
            notificationHelper = NotificationHelper(ApplicationProvider.getApplicationContext()),
            orderNotificationScheduler = scheduler,
        )

        val sellerOrders = sellerRepo.observeOrdersAsSeller().first()

        assertEquals(1, sellerOrders.size)
        assertEquals(orderId, sellerOrders.first().id)
    }

    @Test
    fun confirmOrderAsSeller_movesPendingToConfirmed() = runTest {
        val sellerId = "google:seller-1"
        database.listingDao().insert(
            ListingEntity(
                catalogId = -1,
                title = "Local",
                price = 10.0,
                description = "d",
                category = "electronics",
                imageUri = "uri",
                sellerId = sellerId,
                status = ListingStatus.RESERVED.name,
                createdAt = 1L,
                updatedAt = 1L,
            ),
        )
        val orderId = database.orderDao().insertOrder(
            OrderEntity(
                guestId = "guest",
                totalAmount = 10.0,
                status = OrderStatus.PENDING.name,
                createdAt = 1L,
                meetupLocation = "公园",
            ),
        )
        database.orderDao().insertLineItems(
            listOf(
                OrderLineItemEntity(
                    orderId = orderId,
                    productId = -1,
                    title = "Local",
                    unitPrice = 10.0,
                    quantity = 1,
                    imageUrl = "uri",
                ),
            ),
        )
        val repo = OrderRepositoryImpl(
            orderDao = database.orderDao(),
            cartDao = cartDao,
            authRepository = FakeAuthRepository(initialUserId = sellerId),
            listingRepository = listingRepository,
            notificationHelper = NotificationHelper(ApplicationProvider.getApplicationContext()),
            orderNotificationScheduler = scheduler,
        )

        repo.confirmOrderAsSeller(orderId).getOrThrow()

        val updated = database.orderDao().getById(orderId)
        assertEquals(OrderStatus.CONFIRMED.name, updated?.status)
        assertEquals(listOf(OrderNotificationKind.CONFIRMED_BUYER), scheduler.scheduledKinds)
    }

    @Test
    fun dualMeetupConfirm_completesOrderAndMarksListingSold() = runTest {
        val sellerId = "google:seller-1"
        database.listingDao().insert(
            ListingEntity(
                catalogId = -1,
                title = "Local",
                price = 10.0,
                description = "d",
                category = "electronics",
                imageUri = "uri",
                sellerId = sellerId,
                status = ListingStatus.RESERVED.name,
                createdAt = 1L,
                updatedAt = 1L,
            ),
        )
        val orderId = database.orderDao().insertOrder(
            OrderEntity(
                guestId = "guest",
                totalAmount = 10.0,
                status = OrderStatus.CONFIRMED.name,
                createdAt = 1L,
            ),
        )
        database.orderDao().insertLineItems(
            listOf(
                OrderLineItemEntity(
                    orderId = orderId,
                    productId = -1,
                    title = "Local",
                    unitPrice = 10.0,
                    quantity = 1,
                    imageUrl = "uri",
                ),
            ),
        )
        val buyerRepo = OrderRepositoryImpl(
            orderDao = database.orderDao(),
            cartDao = cartDao,
            authRepository = FakeAuthRepository(),
            listingRepository = listingRepository,
            notificationHelper = NotificationHelper(ApplicationProvider.getApplicationContext()),
            orderNotificationScheduler = scheduler,
        )
        val sellerRepo = OrderRepositoryImpl(
            orderDao = database.orderDao(),
            cartDao = cartDao,
            authRepository = FakeAuthRepository(initialUserId = sellerId),
            listingRepository = listingRepository,
            notificationHelper = NotificationHelper(ApplicationProvider.getApplicationContext()),
            orderNotificationScheduler = scheduler,
        )

        buyerRepo.confirmMeetupAsBuyer(orderId).getOrThrow()
        sellerRepo.confirmMeetupAsSeller(orderId).getOrThrow()

        assertEquals(OrderStatus.COMPLETED.name, database.orderDao().getById(orderId)?.status)
        assertEquals(ListingStatus.SOLD.name, database.listingDao().getByCatalogId(-1)?.status)
        assertEquals(listOf(OrderNotificationKind.COMPLETED), scheduler.scheduledKinds)
    }
}

private class FakeCartDaoForOrder(
    val items: MutableList<CartItemEntity>,
) : CartDao {
    private val state = MutableStateFlow(items.toList())

    override fun observeAll(userId: String): Flow<List<CartItemEntity>> =
        state.map { list -> list.filter { it.userId == userId } }

    override suspend fun getByProductId(userId: String, id: Int): CartItemEntity? =
        items.firstOrNull { it.userId == userId && it.productId == id }

    override suspend fun insert(item: CartItemEntity) {
        items.removeAll { it.userId == item.userId && it.productId == item.productId }
        items.add(item)
        state.value = items.toList()
    }

    override suspend fun updateQuantity(userId: String, id: Int, qty: Int) = Unit

    override suspend fun updateItem(userId: String, id: Int, title: String, price: Double, imageUrl: String, qty: Int) =
        Unit

    override suspend fun deleteByProductId(userId: String, id: Int) {
        items.removeAll { it.userId == userId && it.productId == id }
        state.value = items.toList()
    }

    override suspend fun deleteByProductIdAllUsers(id: Int) {
        items.removeAll { it.productId == id }
        state.value = items.toList()
    }

    override suspend fun clearForUser(userId: String) {
        items.removeAll { it.userId == userId }
        state.value = items.toList()
    }

    override suspend fun migrateGuestCart(newUserId: String) = Unit
}

private class FakeOrderDaoForOrder(
    private val cartDao: FakeCartDaoForOrder,
) : OrderDao {
    var placedLineItems: List<OrderLineItemEntity> = emptyList()
        private set

    override suspend fun insertOrder(order: OrderEntity): Long = 1L

    override suspend fun insertLineItems(items: List<OrderLineItemEntity>) {
        placedLineItems = items
    }

    override suspend fun clearCart(userId: String) {
        cartDao.clearForUser(userId)
    }

    override suspend fun placeOrderWithClearCart(
        order: OrderEntity,
        lineItems: List<OrderLineItemEntity>,
        cartUserId: String,
    ): Long {
        val orderId = insertOrder(order)
        insertLineItems(lineItems.map { it.copy(orderId = orderId) })
        clearCart(cartUserId)
        return orderId
    }

    override fun observeAllOrders(): Flow<List<OrderEntity>> = flowOf(emptyList())

    override fun observeOrdersByUserId(userId: String): Flow<List<OrderEntity>> = flowOf(emptyList())

    override suspend fun migrateGuestOrders(newUserId: String) = Unit

    override fun observeOrder(id: Long): Flow<OrderEntity?> = flowOf(null)

    override fun observeLineItems(orderId: Long): Flow<List<OrderLineItemEntity>> = flowOf(emptyList())

    override fun observeAllLineItems(): Flow<List<OrderLineItemEntity>> = flowOf(emptyList())

    override suspend fun getLocalLineItemProductIds(orderId: Long): List<Int> = emptyList()

    override fun observeOrdersAsSeller(sellerId: String): Flow<List<OrderEntity>> = flowOf(emptyList())

    override suspend fun updateStatus(orderId: Long, status: String) = Unit

    override suspend fun getById(id: Long): OrderEntity? = null

    override suspend fun setBuyerMeetupConfirmed(orderId: Long) = Unit

    override suspend fun setSellerMeetupConfirmed(orderId: Long) = Unit
}

private class RecordingOrderNotificationScheduler : OrderNotificationScheduler {
    val scheduledKinds = mutableListOf<OrderNotificationKind>()

    override fun schedule(
        orderId: Long,
        orderNumber: String,
        kind: OrderNotificationKind,
    ) {
        scheduledKinds.add(kind)
    }
}
