package com.zoti321.c2cmarket.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.zoti321.c2cmarket.data.local.entity.MessageEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Dao
interface MessageDao {
    @Query(
        """
        SELECT * FROM messages
        WHERE conversationId = :conversationId AND status = 'SENT'
        ORDER BY sentAt ASC
        """,
    )
    fun observeSent(conversationId: Long): Flow<List<MessageEntity>>

    @Query(
        """
        SELECT * FROM messages
        WHERE conversationId = :conversationId AND status = 'DRAFT'
        LIMIT 1
        """,
    )
    suspend fun getDraft(conversationId: Long): MessageEntity?

    @Query(
        """
        SELECT * FROM messages
        WHERE conversationId = :conversationId AND status = 'DRAFT'
        LIMIT 1
        """,
    )
    fun observeDraftEntity(conversationId: Long): Flow<MessageEntity?>

    @Insert
    suspend fun insert(entity: MessageEntity): Long

    @Update
    suspend fun update(entity: MessageEntity)

    @Query("DELETE FROM messages WHERE conversationId = :conversationId AND status = 'DRAFT'")
    suspend fun deleteDraft(conversationId: Long)

    @Query(
        """
        UPDATE messages SET isRead = 1
        WHERE conversationId = :conversationId AND senderId != :currentUserId
        """,
    )
    suspend fun markOtherPartyMessagesRead(conversationId: Long, currentUserId: String)

    @Query("SELECT * FROM messages WHERE remoteId = :remoteId LIMIT 1")
    suspend fun findByRemoteId(remoteId: String): MessageEntity?

    @Query("SELECT * FROM messages WHERE syncState = 'PENDING'")
    suspend fun getPendingMessages(): List<MessageEntity>

    @Query("UPDATE messages SET remoteId = :remoteId, syncState = :syncState WHERE id = :id")
    suspend fun updateRemoteSync(id: Long, remoteId: String, syncState: String)
}

fun MessageDao.observeDraft(conversationId: Long): Flow<String> =
    observeDraftEntity(conversationId).map { it?.body.orEmpty() }
