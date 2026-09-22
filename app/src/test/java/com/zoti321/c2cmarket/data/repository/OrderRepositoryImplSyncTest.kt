package com.zoti321.c2cmarket.data.repository

import androidx.test.core.app.ApplicationProvider
import com.zoti321.c2cmarket.data.firebase.OrderStatusTransitions
import com.zoti321.c2cmarket.data.local.InMemoryDatabaseFactory
import com.zoti321.c2cmarket.data.local.entity.ListingEntity
import com.zoti321.c2cmarket.data.local.entity.OrderEntity
import com.zoti321.c2cmarket.domain.model.OrderStatus
import com.zoti321.c2cmarket.domain.model.ShippingInfo
import com.zoti321.c2cmarket.domain.model.UserIds
import com.zoti321.c2cmarket.notification.NotificationHelper
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class OrderRepositoryImplSyncTest {

    private lateinit var database: com.zoti321.c2cmarket.data.local.C2CDatabase
    private lateinit var orderRemote: FakeOrderRemoteDataSource
    private lateinit var deps: RepositoryTestDeps
    private lateinit var scheduler: RecordingOrderNotificationScheduler
    private lateinit var notificationHelper: NotificationHelper

    private val shipping = ShippingInfo("张三", "13800138000", "地址")

    @Before
    fun setUp() {
        database = InMemoryDatabaseFactory.create()
        orderRemote = FakeOrderRemoteDataSource()
        scheduler = RecordingOrderNotificationScheduler()
        notificationHelper = NotificationHelper(ApplicationProvider.getApplicationContext())
        deps = createRepositoryTestDeps().copy(
            orderRemote = orderRemote,
            firebaseAuth = FakeFirebaseAuthGateway(signedIn = true),
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun placeOrder_fakeStoreOrder_doesNotCallRemote() = runTest(deps.dispatcher) {
        val cartDao = FakeCartDaoForOrder(
            mutableListOf(
                com.zoti321.c2cmarket.data.local.entity.CartItemEntity(
                    userId = UserIds.google("buyer-1"),
                    productId = 1,
                    title = "Remote",
                    unitPrice = 10.0,
                    imageUrl = "https://example.com/a.png",
                    quantity = 1,
                    addedAt = 1L,
                ),
            ),
        )
        val repository = createOrderRepository(
            orderDao = FakeOrderDaoForOrder(cartDao),
            cartDao = cartDao,
            listingRepository = createListingRepository(
                database,
                FakeAuthRepository(initialUserId = UserIds.google("buyer-1")),
                deps,
            ),
            authRepository = FakeAuthRepository(initialUserId = UserIds.google("buyer-1")),
            scheduler = scheduler,
            notificationHelper = notificationHelper,
            deps = deps,
        )

        repository.placeOrder(shipping).getOrThrow()

        assertTrue(orderRemote.createdOrders.isEmpty())
    }

    @Test
    fun placeOrder_meetupOrderWhenSignedIn_callsRemoteCreate() = runTest(deps.dispatcher) {
        val sellerId = UserIds.google("seller-1")
        val buyerId = UserIds.google("buyer-1")
        database.listingDao().insert(
            ListingEntity(
                catalogId = -1,
                title = "Local",
                price = 10.0,
                description = "d",
                category = "electronics",
                imageUri = "https://example.com/img.jpg",
                sellerId = sellerId,
                createdAt = 1L,
                updatedAt = 1L,
            ),
        )
        val cartDao = FakeCartDaoForOrder(
            mutableListOf(
                com.zoti321.c2cmarket.data.local.entity.CartItemEntity(
                    userId = buyerId,
                    productId = -1,
                    title = "Local",
                    unitPrice = 10.0,
                    imageUrl = "https://example.com/img.jpg",
                    quantity = 1,
                    addedAt = 1L,
                ),
            ),
        )
        val repository = createOrderRepository(
            orderDao = FakeOrderDaoForOrder(cartDao),
            cartDao = cartDao,
            listingRepository = createListingRepository(
                database,
                FakeAuthRepository(initialUserId = sellerId),
                deps,
            ),
            authRepository = FakeAuthRepository(initialUserId = buyerId),
            scheduler = scheduler,
            notificationHelper = notificationHelper,
            deps = deps,
        )

        repository.placeOrder(shipping).getOrThrow()

        assertEquals(1, orderRemote.createdOrders.size)
    }

    @Test
    fun orderStatusTransitions_rejectsCompletedRollback() {
        assertFalse(OrderStatusTransitions.canTransition(OrderStatus.COMPLETED, OrderStatus.PENDING))
        assertFalse(OrderStatusTransitions.canTransition(OrderStatus.CANCELLED, OrderStatus.CONFIRMED))
    }
}

