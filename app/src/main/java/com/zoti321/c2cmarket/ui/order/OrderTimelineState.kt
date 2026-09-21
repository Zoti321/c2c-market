package com.zoti321.c2cmarket.ui.order

import com.zoti321.c2cmarket.domain.model.Order
import com.zoti321.c2cmarket.domain.model.OrderStatus

internal data class OrderTimelineState(
    val pendingDone: Boolean,
    val pendingActive: Boolean,
    val confirmedDone: Boolean,
    val confirmedActive: Boolean,
    val completedDone: Boolean,
    val completedActive: Boolean,
    val cancelled: Boolean,
)

internal fun orderTimelineState(order: Order): OrderTimelineState {
    val status = order.status
    return OrderTimelineState(
        pendingDone = status != OrderStatus.PENDING,
        pendingActive = status == OrderStatus.PENDING,
        confirmedDone = status == OrderStatus.COMPLETED,
        confirmedActive = status == OrderStatus.CONFIRMED,
        completedDone = status == OrderStatus.COMPLETED,
        completedActive = status == OrderStatus.COMPLETED,
        cancelled = status == OrderStatus.CANCELLED,
    )
}
