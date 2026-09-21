package com.zoti321.c2cmarket.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.zoti321.c2cmarket.data.local.entity.OrderEntity
import com.zoti321.c2cmarket.data.local.entity.OrderLineItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
@Suppress("TooManyFunctions")
interface OrderDao {
    @Insert
    suspend fun insertOrder(order: OrderEntity): Long

    @Insert
    suspend fun insertLineItems(items: List<OrderLineItemEntity>)

    @Query("DELETE FROM cart_items WHERE userId = :userId")
    suspend fun clearCart(userId: String)

    @Transaction
    suspend fun placeOrderWithClearCart(
        order: OrderEntity,
        lineItems: List<OrderLineItemEntity>,
        cartUserId: String,
    ): Long {
        val orderId = insertOrder(order)
        insertLineItems(lineItems.map { it.copy(orderId = orderId) })
        clearCart(cartUserId)
        return orderId
    }

    @Query("SELECT * FROM orders ORDER BY createdAt DESC")
    fun observeAllOrders(): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE guestId = :userId ORDER BY createdAt DESC")
    fun observeOrdersByUserId(userId: String): Flow<List<OrderEntity>>

    @Query("UPDATE orders SET guestId = :newUserId WHERE guestId = 'guest'")
    suspend fun migrateGuestOrders(newUserId: String)

    @Query("SELECT * FROM orders WHERE id = :id")
    fun observeOrder(id: Long): Flow<OrderEntity?>

    @Query("SELECT * FROM order_line_items WHERE orderId = :orderId")
    fun observeLineItems(orderId: Long): Flow<List<OrderLineItemEntity>>

    @Query("SELECT * FROM order_line_items")
    fun observeAllLineItems(): Flow<List<OrderLineItemEntity>>

    @Query("SELECT productId FROM order_line_items WHERE orderId = :orderId AND productId < 0")
    suspend fun getLocalLineItemProductIds(orderId: Long): List<Int>

    @Query(
        """
        SELECT DISTINCT o.* FROM orders o
        INNER JOIN order_line_items li ON li.orderId = o.id
        INNER JOIN listings l ON l.catalogId = li.productId AND li.productId < 0
        WHERE l.sellerId = :sellerId
        ORDER BY o.createdAt DESC
        """,
    )
    fun observeOrdersAsSeller(sellerId: String): Flow<List<OrderEntity>>

    @Query("UPDATE orders SET status = :status WHERE id = :orderId")
    suspend fun updateStatus(orderId: Long, status: String)

    @Query("SELECT * FROM orders WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): OrderEntity?

    @Query("UPDATE orders SET buyerMeetupConfirmed = 1 WHERE id = :orderId")
    suspend fun setBuyerMeetupConfirmed(orderId: Long)

    @Query("UPDATE orders SET sellerMeetupConfirmed = 1 WHERE id = :orderId")
    suspend fun setSellerMeetupConfirmed(orderId: Long)
}
