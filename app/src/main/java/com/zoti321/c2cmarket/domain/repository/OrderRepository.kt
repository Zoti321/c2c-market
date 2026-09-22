package com.zoti321.c2cmarket.domain.repository

import com.zoti321.c2cmarket.domain.model.Order
import com.zoti321.c2cmarket.domain.model.OrderSummary
import com.zoti321.c2cmarket.domain.model.ShippingInfo
import kotlinx.coroutines.flow.Flow

@Suppress("TooManyFunctions")
interface OrderRepository {
    suspend fun placeOrder(shipping: ShippingInfo): Result<Order>

    fun observeOrders(): Flow<List<OrderSummary>>

    fun observeOrdersAsSeller(): Flow<List<OrderSummary>>

    fun observeOrder(orderId: Long): Flow<Order?>

    suspend fun isSellerForOrder(orderId: Long): Boolean

    suspend fun confirmOrderAsSeller(orderId: Long): Result<Unit>

    suspend fun confirmMeetupAsBuyer(orderId: Long): Result<Unit>

    suspend fun confirmMeetupAsSeller(orderId: Long): Result<Unit>

    suspend fun cancelOrderAsSeller(orderId: Long): Result<Unit>

    suspend fun resolveLocalId(remoteOrderId: String): Long?

    suspend fun retryPendingUploads()
}
