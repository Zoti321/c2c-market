package com.zoti321.c2cmarket.data.remote

import com.zoti321.c2cmarket.domain.datasource.OrderRemoteDataSource
import com.zoti321.c2cmarket.domain.datasource.OrderSyncExtras
import com.zoti321.c2cmarket.domain.datasource.RemoteOrder
import com.zoti321.c2cmarket.domain.model.Order
import com.zoti321.c2cmarket.domain.model.OrderStatus
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

@Singleton
class NoOpOrderRemoteDataSource @Inject constructor() : OrderRemoteDataSource {
    override suspend fun createOrder(order: Order, remoteId: String, sellerId: String): Result<Unit> =
        Result.success(Unit)

    override suspend fun syncOrderStatus(
        remoteId: String,
        status: OrderStatus,
        extras: OrderSyncExtras,
    ): Result<Unit> = Result.success(Unit)

    override suspend fun syncMeetupConfirm(
        remoteId: String,
        buyerConfirmed: Boolean,
        sellerConfirmed: Boolean,
    ): Result<Unit> = Result.success(Unit)

    override fun observeRemoteOrders(userId: String): Flow<List<RemoteOrder>> = flowOf(emptyList())

    override suspend fun retryPendingUploads() = Unit
}
