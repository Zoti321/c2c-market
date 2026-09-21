package com.zoti321.c2cmarket.data.sync.merger

import com.zoti321.c2cmarket.data.firebase.OrderStatusTransitions
import com.zoti321.c2cmarket.data.local.dao.OrderDao
import com.zoti321.c2cmarket.data.local.entity.OrderEntity
import com.zoti321.c2cmarket.data.local.entity.OrderLineItemEntity
import com.zoti321.c2cmarket.data.local.entity.SyncStateValues
import com.zoti321.c2cmarket.domain.datasource.RemoteOrder
import com.zoti321.c2cmarket.domain.model.OrderStatus
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OrderRemoteMerger @Inject constructor(
    private val orderDao: OrderDao,
) {
    suspend fun mergeAll(remote: List<RemoteOrder>) {
        remote.forEach { item ->
            val existing = orderDao.findByRemoteId(item.remoteId)
            if (existing == null) {
                insertRemoteOrder(item)
            } else {
                mergeExisting(existing, item)
            }
        }
    }

    private suspend fun insertRemoteOrder(item: RemoteOrder) {
        val orderId = orderDao.insertOrder(
            OrderEntity(
                guestId = item.buyerId,
                totalAmount = item.totalAmount,
                status = item.status.name,
                createdAt = item.createdAt,
                meetupLocation = item.meetupLocation,
                buyerMeetupConfirmed = item.buyerMeetupConfirmed,
                sellerMeetupConfirmed = item.sellerMeetupConfirmed,
                remoteId = item.remoteId,
                syncState = SyncStateValues.SYNCED,
            ),
        )
        orderDao.insertLineItems(
            item.lineItems.map { line ->
                OrderLineItemEntity(
                    orderId = orderId,
                    productId = line.productId,
                    title = line.title,
                    unitPrice = line.unitPrice,
                    quantity = line.quantity,
                    imageUrl = line.imageUrl,
                )
            },
        )
    }

    private suspend fun mergeExisting(existing: OrderEntity, item: RemoteOrder) {
        val currentStatus = runCatching { OrderStatus.valueOf(existing.status) }
            .getOrDefault(OrderStatus.PENDING)
        val mergedStatus = if (OrderStatusTransitions.canTransition(currentStatus, item.status)) {
            item.status.name
        } else {
            existing.status
        }
        orderDao.updateRemoteSync(existing.id, item.remoteId, SyncStateValues.SYNCED)
        orderDao.updateStatus(existing.id, mergedStatus)
    }
}
