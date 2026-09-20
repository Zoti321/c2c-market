package com.zoti321.c2cmarket.domain.scheduler

interface OrderNotificationScheduler {
    fun schedule(orderId: Long, orderNumber: String, kind: OrderNotificationKind = OrderNotificationKind.SHIPPED)
}
