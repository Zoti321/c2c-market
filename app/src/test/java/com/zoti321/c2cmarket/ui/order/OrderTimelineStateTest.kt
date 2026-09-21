package com.zoti321.c2cmarket.ui.order

import com.zoti321.c2cmarket.domain.GuestSession
import com.zoti321.c2cmarket.domain.model.Order
import com.zoti321.c2cmarket.domain.model.OrderLineItem
import com.zoti321.c2cmarket.domain.model.OrderStatus
import java.time.Instant
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OrderTimelineStateTest {

    @Test
    fun pendingOrder_marksPendingActive() {
        val timeline = orderTimelineState(sampleOrder(OrderStatus.PENDING))

        assertTrue(timeline.pendingActive)
        assertFalse(timeline.pendingDone)
    }

    @Test
    fun cancelledOrder_marksPendingDoneWithoutUsingOrdinal() {
        val timeline = orderTimelineState(sampleOrder(OrderStatus.CANCELLED))

        assertTrue(timeline.pendingDone)
        assertTrue(timeline.cancelled)
        assertFalse(timeline.confirmedActive)
    }

    @Test
    fun completedOrder_marksAllStepsDone() {
        val timeline = orderTimelineState(sampleOrder(OrderStatus.COMPLETED))

        assertTrue(timeline.pendingDone)
        assertTrue(timeline.confirmedDone)
        assertTrue(timeline.completedDone)
        assertTrue(timeline.completedActive)
    }

    private fun sampleOrder(status: OrderStatus) = Order(
        id = 1L,
        guestId = GuestSession.GUEST_ID,
        items = listOf(
            OrderLineItem(
                productId = -1,
                title = "Local",
                unitPrice = 10.0,
                quantity = 1,
                imageUrl = "uri",
            ),
        ),
        totalAmount = 10.0,
        status = status,
        createdAt = Instant.ofEpochMilli(1L),
    )
}
