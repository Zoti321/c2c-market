package com.zoti321.c2cmarket.domain.datasource

import com.zoti321.c2cmarket.domain.model.Order
import com.zoti321.c2cmarket.domain.model.OrderStatus
import kotlinx.coroutines.flow.Flow

interface OrderRemoteDataSource {
    suspend fun createOrder(order: Order, remoteId: String, sellerId: String): Result<Unit>

    suspend fun syncOrderStatus(
        remoteId: String,
        status: OrderStatus,
        extras: OrderSyncExtras = OrderSyncExtras(),
    ): Result<Unit>

    suspend fun syncMeetupConfirm(
        remoteId: String,
        buyerConfirmed: Boolean,
        sellerConfirmed: Boolean,
    ): Result<Unit>

    fun observeRemoteOrders(userId: String): Flow<List<RemoteOrder>>

    suspend fun retryPendingUploads()
}
