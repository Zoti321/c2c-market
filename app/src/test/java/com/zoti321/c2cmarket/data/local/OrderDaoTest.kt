package com.zoti321.c2cmarket.data.local

import androidx.test.core.app.ApplicationProvider
import com.zoti321.c2cmarket.data.local.entity.CartItemEntity
import com.zoti321.c2cmarket.data.local.entity.OrderEntity
import com.zoti321.c2cmarket.data.local.entity.OrderLineItemEntity
import com.zoti321.c2cmarket.domain.GuestSession
import com.zoti321.c2cmarket.domain.model.OrderStatus
import kotlinx.coroutines.flow.first
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
class OrderDaoTest {

    private lateinit var database: C2CDatabase

    @Before
    fun setUp() {
        database = InMemoryDatabaseFactory.create()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun placeOrderWithClearCart_persistsOrderAndClearsCart() = runTest {
        val cartDao = database.cartDao()
        cartDao.insert(
            CartItemEntity(
                userId = GuestSession.GUEST_ID,
                productId = 1,
                title = "Phone",
                unitPrice = 12.0,
                imageUrl = "https://example.com/a.png",
                quantity = 2,
                addedAt = 1L,
            ),
        )
        val orderDao = database.orderDao()
        val order = OrderEntity(
            guestId = GuestSession.GUEST_ID,
            totalAmount = 24.0,
            status = OrderStatus.COMPLETED.name,
            createdAt = 100L,
            shippingReceiverName = "张三",
            shippingPhone = "13800138000",
            shippingAddress = "广东省深圳市南山区 科技园",
        )
        val lines = listOf(
            OrderLineItemEntity(
                orderId = 0,
                productId = 1,
                title = "Phone",
                unitPrice = 12.0,
                quantity = 2,
                imageUrl = "https://example.com/a.png",
            ),
        )

        val orderId = orderDao.placeOrderWithClearCart(order, lines, GuestSession.GUEST_ID)

        assertTrue(orderId > 0)
        assertTrue(cartDao.observeAll(GuestSession.GUEST_ID).first().isEmpty())
        val savedLines = orderDao.observeLineItems(orderId).first()
        assertEquals(1, savedLines.size)
        assertEquals(orderId, savedLines.first().orderId)
    }
}
