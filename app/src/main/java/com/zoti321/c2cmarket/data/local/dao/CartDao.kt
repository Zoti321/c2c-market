package com.zoti321.c2cmarket.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.zoti321.c2cmarket.data.local.entity.CartItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CartDao {
    @Query("SELECT * FROM cart_items ORDER BY addedAt DESC")
    fun observeAll(): Flow<List<CartItemEntity>>

    @Query("SELECT * FROM cart_items WHERE productId = :id")
    suspend fun getByProductId(id: Int): CartItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: CartItemEntity)

    @Query("UPDATE cart_items SET quantity = :qty WHERE productId = :id")
    suspend fun updateQuantity(id: Int, qty: Int)

    @Query(
        "UPDATE cart_items SET title = :title, unitPrice = :price, " +
            "imageUrl = :imageUrl, quantity = :qty WHERE productId = :id",
    )
    suspend fun updateItem(id: Int, title: String, price: Double, imageUrl: String, qty: Int)

    @Query("DELETE FROM cart_items WHERE productId = :id")
    suspend fun deleteByProductId(id: Int)

    @Query("DELETE FROM cart_items")
    suspend fun clearAll()
}
