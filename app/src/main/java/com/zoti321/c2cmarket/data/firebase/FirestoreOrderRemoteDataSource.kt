package com.zoti321.c2cmarket.data.firebase

import com.google.firebase.firestore.FirebaseFirestore
import com.zoti321.c2cmarket.domain.datasource.OrderRemoteDataSource
import com.zoti321.c2cmarket.domain.datasource.OrderSyncExtras
import com.zoti321.c2cmarket.domain.datasource.RemoteOrder
import com.zoti321.c2cmarket.domain.datasource.RemoteOrderLineItem
import com.zoti321.c2cmarket.domain.model.Order
import com.zoti321.c2cmarket.domain.model.OrderStatus
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

@Singleton
class FirestoreOrderRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
) : OrderRemoteDataSource {

    override suspend fun createOrder(order: Order, remoteId: String, sellerId: String): Result<Unit> =
        runCatching {
            firestore.collection(COLLECTION_ORDERS)
                .document(remoteId)
                .set(order.toRemoteMap(sellerId))
                .await()
        }

    override suspend fun syncOrderStatus(
        remoteId: String,
        status: OrderStatus,
        extras: OrderSyncExtras,
    ): Result<Unit> = runCatching {
        firestore.collection(COLLECTION_ORDERS)
            .document(remoteId)
            .update(
                buildMap {
                    put(FIELD_STATUS, status.name)
                    put(FIELD_UPDATED_AT, System.currentTimeMillis())
                    if (extras.buyerMeetupConfirmed) put(FIELD_BUYER_MEETUP_CONFIRMED, true)
                    if (extras.sellerMeetupConfirmed) put(FIELD_SELLER_MEETUP_CONFIRMED, true)
                    extras.meetupLocation?.let { put(FIELD_MEETUP_LOCATION, it) }
                },
            )
            .await()
    }

    override suspend fun syncMeetupConfirm(
        remoteId: String,
        buyerConfirmed: Boolean,
        sellerConfirmed: Boolean,
    ): Result<Unit> = runCatching {
        firestore.collection(COLLECTION_ORDERS)
            .document(remoteId)
            .update(
                mapOf(
                    FIELD_BUYER_MEETUP_CONFIRMED to buyerConfirmed,
                    FIELD_SELLER_MEETUP_CONFIRMED to sellerConfirmed,
                    FIELD_UPDATED_AT to System.currentTimeMillis(),
                ),
            )
            .await()
    }

    override fun observeRemoteOrders(userId: String): Flow<List<RemoteOrder>> = callbackFlow {
        val buyerMap = mutableMapOf<String, RemoteOrder>()
        val sellerMap = mutableMapOf<String, RemoteOrder>()

        fun emitMerged() {
            val merged = (buyerMap.values + sellerMap.values)
                .distinctBy { it.remoteId }
            trySend(merged)
        }

        val buyerRegistration = firestore.collection(COLLECTION_ORDERS)
            .whereEqualTo(FIELD_BUYER_ID, userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                buyerMap.clear()
                snapshot?.documents.orEmpty().forEach { doc ->
                    doc.toRemoteOrder()?.let { buyerMap[it.remoteId] = it }
                }
                emitMerged()
            }
        val sellerRegistration = firestore.collection(COLLECTION_ORDERS)
            .whereEqualTo(FIELD_SELLER_ID, userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                sellerMap.clear()
                snapshot?.documents.orEmpty().forEach { doc ->
                    doc.toRemoteOrder()?.let { sellerMap[it.remoteId] = it }
                }
                emitMerged()
            }
        awaitClose {
            buyerRegistration.remove()
            sellerRegistration.remove()
        }
    }

    override suspend fun retryPendingUploads() = Unit

    private fun Order.toRemoteMap(sellerId: String): Map<String, Any?> = mapOf(
            FIELD_BUYER_ID to guestId,
            FIELD_SELLER_ID to sellerId,
            FIELD_STATUS to status.name,
            FIELD_LINE_ITEMS to items.map { item ->
                mapOf(
                    FIELD_PRODUCT_ID to item.productId,
                    FIELD_TITLE to item.title,
                    FIELD_UNIT_PRICE to item.unitPrice,
                    FIELD_QUANTITY to item.quantity,
                    FIELD_IMAGE_URL to item.imageUrl,
                )
            },
            FIELD_TOTAL_AMOUNT to totalAmount,
            FIELD_MEETUP_LOCATION to meetupLocation,
            FIELD_BUYER_MEETUP_CONFIRMED to buyerMeetupConfirmed,
            FIELD_SELLER_MEETUP_CONFIRMED to sellerMeetupConfirmed,
            FIELD_CREATED_AT to createdAt.toEpochMilli(),
            FIELD_UPDATED_AT to System.currentTimeMillis(),
        )

    @Suppress("UNCHECKED_CAST")
    private fun com.google.firebase.firestore.DocumentSnapshot.toRemoteOrder(): RemoteOrder? {
        val remoteId = id
        val buyerId = getString(FIELD_BUYER_ID) ?: return null
        val sellerId = getString(FIELD_SELLER_ID) ?: return null
        val statusName = getString(FIELD_STATUS) ?: return null
        val status = runCatching { OrderStatus.valueOf(statusName) }.getOrNull() ?: return null
        val lineItemsRaw = get(FIELD_LINE_ITEMS) as? List<Map<String, Any>> ?: emptyList()
        val lineItems = lineItemsRaw.mapNotNull { map ->
            val productId = (map[FIELD_PRODUCT_ID] as? Number)?.toInt() ?: return@mapNotNull null
            RemoteOrderLineItem(
                productId = productId,
                title = map[FIELD_TITLE] as? String ?: "",
                unitPrice = (map[FIELD_UNIT_PRICE] as? Number)?.toDouble() ?: 0.0,
                quantity = (map[FIELD_QUANTITY] as? Number)?.toInt() ?: 1,
                imageUrl = map[FIELD_IMAGE_URL] as? String ?: "",
            )
        }
        return RemoteOrder(
            remoteId = remoteId,
            buyerId = buyerId,
            sellerId = sellerId,
            status = status,
            lineItems = lineItems,
            totalAmount = getDouble(FIELD_TOTAL_AMOUNT) ?: 0.0,
            meetupLocation = getString(FIELD_MEETUP_LOCATION),
            buyerMeetupConfirmed = getBoolean(FIELD_BUYER_MEETUP_CONFIRMED) ?: false,
            sellerMeetupConfirmed = getBoolean(FIELD_SELLER_MEETUP_CONFIRMED) ?: false,
            createdAt = getLong(FIELD_CREATED_AT) ?: 0L,
            updatedAt = getLong(FIELD_UPDATED_AT) ?: 0L,
        )
    }

    companion object {
        const val COLLECTION_ORDERS = "orders"
        const val FIELD_BUYER_ID = "buyerId"
        const val FIELD_SELLER_ID = "sellerId"
        const val FIELD_STATUS = "status"
        const val FIELD_LINE_ITEMS = "lineItems"
        const val FIELD_PRODUCT_ID = "productId"
        const val FIELD_TITLE = "title"
        const val FIELD_UNIT_PRICE = "unitPrice"
        const val FIELD_QUANTITY = "quantity"
        const val FIELD_IMAGE_URL = "imageUrl"
        const val FIELD_TOTAL_AMOUNT = "totalAmount"
        const val FIELD_MEETUP_LOCATION = "meetupLocation"
        const val FIELD_BUYER_MEETUP_CONFIRMED = "buyerMeetupConfirmed"
        const val FIELD_SELLER_MEETUP_CONFIRMED = "sellerMeetupConfirmed"
        const val FIELD_CREATED_AT = "createdAt"
        const val FIELD_UPDATED_AT = "updatedAt"
    }
}
