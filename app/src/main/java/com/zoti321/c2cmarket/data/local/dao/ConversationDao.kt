package com.zoti321.c2cmarket.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.zoti321.c2cmarket.data.local.entity.ConversationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ConversationDao {
    @Query("SELECT * FROM conversations WHERE buyerId = :buyerId ORDER BY lastMessageAt DESC")
    fun observeByBuyerId(buyerId: String): Flow<List<ConversationEntity>>

    @Query("SELECT * FROM conversations WHERE sellerId = :sellerId ORDER BY lastMessageAt DESC")
    fun observeBySellerId(sellerId: String): Flow<List<ConversationEntity>>

    @Query(
        """
        SELECT * FROM conversations
        WHERE buyerId = :buyerId AND sellerId = :sellerId AND productId = :productId
        LIMIT 1
        """,
    )
    suspend fun findByUniqueKey(
        buyerId: String,
        sellerId: String,
        productId: Int,
    ): ConversationEntity?

    @Query("SELECT * FROM conversations WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): ConversationEntity?

    @Query("SELECT * FROM conversations WHERE id = :id LIMIT 1")
    fun observeById(id: Long): Flow<ConversationEntity?>

    @Insert
    suspend fun insert(entity: ConversationEntity): Long

    @Update
    suspend fun update(entity: ConversationEntity)

    @Query("UPDATE conversations SET unreadCount = 0 WHERE id = :conversationId")
    suspend fun clearBuyerUnread(conversationId: Long)

    @Query("UPDATE conversations SET sellerUnreadCount = 0 WHERE id = :conversationId")
    suspend fun clearSellerUnread(conversationId: Long)

    @Query("UPDATE conversations SET buyerId = :newUserId WHERE buyerId = 'guest'")
    suspend fun migrateGuestConversations(newUserId: String)

    @Query(
        """
        UPDATE conversations
        SET lastMessagePreview = :preview, lastMessageAt = :lastMessageAt
        WHERE id = :id
        """,
    )
    suspend fun updatePreview(
        id: Long,
        preview: String,
        lastMessageAt: Long,
    )

    @Query("UPDATE conversations SET unreadCount = unreadCount + 1 WHERE id = :conversationId")
    suspend fun incrementBuyerUnread(conversationId: Long)

    @Query("UPDATE conversations SET sellerUnreadCount = sellerUnreadCount + 1 WHERE id = :conversationId")
    suspend fun incrementSellerUnread(conversationId: Long)
}
