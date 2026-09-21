package com.zoti321.c2cmarket.data.sync.merger

import com.zoti321.c2cmarket.data.local.InMemoryDatabaseFactory
import com.zoti321.c2cmarket.data.local.entity.OrderEntity
import com.zoti321.c2cmarket.domain.datasource.RemoteOrder
import com.zoti321.c2cmarket.domain.datasource.RemoteOrderLineItem
import com.zoti321.c2cmarket.domain.model.OrderStatus
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class OrderRemoteMergerTest {

    private lateinit var database: com.zoti321.c2cmarket.data.local.C2CDatabase
    private lateinit var merger: OrderRemoteMerger

    @Before
    fun setUp() {
        database = InMemoryDatabaseFactory.create()
        merger = OrderRemoteMerger(database.orderDao())
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun mergeAll_rejectsIllegalStatusRegression() = runTest {
        val orderDao = database.orderDao()
        val localId = orderDao.insertOrder(
            OrderEntity(
                guestId = "google:buyer",
                totalAmount = 10.0,
                status = OrderStatus.COMPLETED.name,
                createdAt = 1L,
                remoteId = "remote-1",
            ),
        )
        orderDao.insertLineItems(
            listOf(
                com.zoti321.c2cmarket.data.local.entity.OrderLineItemEntity(
                    orderId = localId,
                    productId = -1,
                    title = "Item",
                    unitPrice = 10.0,
                    quantity = 1,
                    imageUrl = "https://example.com/a.jpg",
                ),
            ),
        )

        merger.mergeAll(
            listOf(
                RemoteOrder(
                    remoteId = "remote-1",
                    buyerId = "google:buyer",
                    sellerId = "google:seller",
                    status = OrderStatus.PENDING,
                    lineItems = listOf(
                        RemoteOrderLineItem(-1, "Item", 10.0, 1, "https://example.com/a.jpg"),
                    ),
                    totalAmount = 10.0,
                    meetupLocation = null,
                    buyerMeetupConfirmed = false,
                    sellerMeetupConfirmed = false,
                    createdAt = 1L,
                    updatedAt = 2L,
                ),
            ),
        )

        assertEquals(OrderStatus.COMPLETED.name, orderDao.getById(localId)?.status)
    }
}
