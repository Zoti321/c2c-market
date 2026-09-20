package com.zoti321.c2cmarket.ui.common

import com.zoti321.c2cmarket.R
import com.zoti321.c2cmarket.domain.model.OrderStatus

fun orderStatusLabelRes(status: OrderStatus): Int = when (status) {
    OrderStatus.PENDING -> R.string.order_status_pending
    OrderStatus.CONFIRMED -> R.string.order_status_confirmed
    OrderStatus.COMPLETED -> R.string.order_status_completed
    OrderStatus.CANCELLED -> R.string.order_status_cancelled
}
