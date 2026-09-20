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
    fun observeAll(buyerId: String): Flow<List<ConversationEntity>>

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

    @Insert
    suspend fun insert(entity: ConversationEntity): Long

    @Update
    suspend fun update(entity: ConversationEntity)

    @Query("UPDATE conversations SET unreadCount = 0 WHERE id = :conversationId")
    suspend fun clearUnread(conversationId: Long)

    @Query("UPDATE conversations SET buyerId = :newUserId WHERE buyerId = 'guest'")
    suspend fun migrateGuestConversations(newUserId: String)

    @Query(
        """
        UPDATE conversations
        SET lastMessagePreview = :preview, lastMessageAt = :lastMessageAt, unreadCount = :unreadCount
        WHERE id = :id
        """,
    )
    suspend fun updatePreview(
        id: Long,
        preview: String,
        lastMessageAt: Long,
        unreadCount: Int,
    )
}
