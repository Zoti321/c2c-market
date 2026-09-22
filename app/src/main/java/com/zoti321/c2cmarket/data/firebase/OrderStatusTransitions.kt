package com.zoti321.c2cmarket.data.firebase

import com.zoti321.c2cmarket.domain.model.OrderStatus

object OrderStatusTransitions {
    private val allowed = mapOf(
        OrderStatus.PENDING to setOf(OrderStatus.CONFIRMED, OrderStatus.CANCELLED),
        OrderStatus.CONFIRMED to setOf(OrderStatus.COMPLETED, OrderStatus.CANCELLED),
        OrderStatus.COMPLETED to emptySet(),
        OrderStatus.CANCELLED to emptySet(),
    )

    fun canTransition(from: OrderStatus, to: OrderStatus): Boolean =
        from == to || allowed[from]?.contains(to) == true
}
