package com.zoti321.c2cmarket.data.repository

import androidx.test.core.app.ApplicationProvider
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
import com.zoti321.c2cmarket.domain.model.UserIds
import com.zoti321.c2cmarket.domain.scheduler.OrderNotificationKind
import com.zoti321.c2cmarket.domain.scheduler.OrderNotificationScheduler
import com.zoti321.c2cmarket.notification.NotificationHelper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
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
    private lateinit var deps: RepositoryTestDeps
    private lateinit var notificationHelper: NotificationHelper

    private val shipping = ShippingInfo(
        receiverName = "张三",
        phone = "13800138000",
        address = "广东省深圳市南山区 科技园",
    )

    @Before
    fun setUp() {
        database = InMemoryDatabaseFactory.create()
        deps = createRepositoryTestDeps()
        notificationHelper = NotificationHelper(ApplicationProvider.getApplicationContext())
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
        listingRepository = createListingRepository(database, deps = deps)
        repository = createOrderRepository(
            orderDao = orderDao,
            cartDao = cartDao,
            listingRepository = listingRepository,
            scheduler = scheduler,
            notificationHelper = notificationHelper,
            deps = deps,
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun placeOrder_withCartItems_clearsCartAndPersistsShipping() = runTest(deps.dispatcher) {
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
    fun placeOrder_emptyCart_fails() = runTest(deps.dispatcher) {
        cartDao.clearForUser("guest")

        val result = repository.placeOrder(shipping)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is EmptyCartException)
        assertTrue(scheduler.scheduledKinds.isEmpty())
    }

    @Test
    fun placeOrder_withLocalListing_marksListingReserved_withoutWorkManager() = runTest(deps.dispatcher) {
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
        repository = createOrderRepository(
            orderDao = orderDao,
            cartDao = cartDao,
            listingRepository = listingRepository,
            scheduler = scheduler,
            notificationHelper = notificationHelper,
            deps = deps,
        )

        repository.placeOrder(shipping).getOrThrow()

        val listing = database.listingDao().getByCatalogId(-1)
        assertEquals(ListingStatus.RESERVED.name, listing?.status)
        assertTrue(scheduler.scheduledKinds.isEmpty())
    }

    @Test
    fun placeOrder_meetupWhenSignedIn_createsRemoteOrder() = runTest(deps.dispatcher) {
        val signedInDeps = deps.copy(
            firebaseAuth = FakeFirebaseAuthGateway(signedIn = true),
        )
        val sellerId = UserIds.google("seller-1")
        database.listingDao().insert(
            ListingEntity(
                catalogId = -1,
                title = "Local Item",
                price = 50.0,
                description = "Desc",
                category = "electronics",
                imageUri = "https://example.com/img.jpg",
                sellerId = sellerId,
                createdAt = 1L,
                updatedAt = 1L,
            ),
        )
        cartDao = FakeCartDaoForOrder(
            mutableListOf(
                CartItemEntity(
                    userId = sellerId,
                    productId = -1,
                    title = "Local Item",
                    unitPrice = 50.0,
                    imageUrl = "https://example.com/img.jpg",
                    quantity = 1,
                    addedAt = 1L,
                ),
            ),
        )
        orderDao = FakeOrderDaoForOrder(cartDao)
        repository = createOrderRepository(
            orderDao = orderDao,
            cartDao = cartDao,
            listingRepository = createListingRepository(
                database,
                FakeAuthRepository(initialUserId = sellerId),
                signedInDeps,
            ),
            authRepository = FakeAuthRepository(initialUserId = sellerId),
            scheduler = scheduler,
            notificationHelper = notificationHelper,
            deps = signedInDeps,
        )

        repository.placeOrder(shipping).getOrThrow()

        assertEquals(1, signedInDeps.orderRemote.createdOrders.size)
        assertTrue(scheduler.scheduledKinds.isEmpty())
    }

    @Test
    fun observeOrdersAsSeller_returnsOrdersWithSellerListings() = runTest(deps.dispatcher) {
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
        val sellerRepo = createOrderRepository(
            orderDao = database.orderDao(),
            cartDao = cartDao,
            listingRepository = listingRepository,
            authRepository = FakeAuthRepository(initialUserId = sellerId),
            scheduler = scheduler,
            notificationHelper = notificationHelper,
            deps = deps,
        )

        val sellerOrders = sellerRepo.observeOrdersAsSeller().first()

        assertEquals(1, sellerOrders.size)
        assertEquals(orderId, sellerOrders.first().id)
    }

    @Test
    fun confirmOrderAsSeller_movesPendingToConfirmed_withoutWorkManager() = runTest(deps.dispatcher) {
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
        val repo = createOrderRepository(
            orderDao = database.orderDao(),
            cartDao = cartDao,
            listingRepository = listingRepository,
            authRepository = FakeAuthRepository(initialUserId = sellerId),
            scheduler = scheduler,
            notificationHelper = notificationHelper,
            deps = deps,
        )

        repo.confirmOrderAsSeller(orderId).getOrThrow()

        val updated = database.orderDao().getById(orderId)
        assertEquals(OrderStatus.CONFIRMED.name, updated?.status)
        assertTrue(scheduler.scheduledKinds.isEmpty())
    }

    @Test
    fun dualMeetupConfirm_completesOrderAndMarksListingSold_withoutWorkManager() = runTest(deps.dispatcher) {
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
        val buyerRepo = createOrderRepository(
            orderDao = database.orderDao(),
            cartDao = cartDao,
            listingRepository = listingRepository,
            scheduler = scheduler,
            notificationHelper = notificationHelper,
            deps = deps,
        )
        val sellerRepo = createOrderRepository(
            orderDao = database.orderDao(),
            cartDao = cartDao,
            listingRepository = listingRepository,
            authRepository = FakeAuthRepository(initialUserId = sellerId),
            scheduler = scheduler,
            notificationHelper = notificationHelper,
            deps = deps,
        )

        buyerRepo.confirmMeetupAsBuyer(orderId).getOrThrow()
        sellerRepo.confirmMeetupAsSeller(orderId).getOrThrow()

        assertEquals(OrderStatus.COMPLETED.name, database.orderDao().getById(orderId)?.status)
        assertEquals(ListingStatus.SOLD.name, database.listingDao().getByCatalogId(-1)?.status)
        assertTrue(scheduler.scheduledKinds.isEmpty())
    }
}
