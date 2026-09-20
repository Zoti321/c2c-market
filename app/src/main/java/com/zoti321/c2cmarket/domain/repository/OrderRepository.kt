package com.zoti321.c2cmarket.domain.repository

import com.zoti321.c2cmarket.domain.model.Order
import com.zoti321.c2cmarket.domain.model.OrderSummary
import com.zoti321.c2cmarket.domain.model.ShippingInfo
import kotlinx.coroutines.flow.Flow

interface OrderRepository {
    suspend fun placeOrder(shipping: ShippingInfo): Result<Order>

    fun observeOrders(): Flow<List<OrderSummary>>

    fun observeOrder(orderId: Long): Flow<Order?>
}
