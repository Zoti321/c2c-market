package com.zoti321.c2cmarket.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "messages",
    foreignKeys = [
        ForeignKey(
            entity = ConversationEntity::class,
            parentColumns = ["id"],
            childColumns = ["conversationId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["conversationId", "status"]),
        Index(value = ["remoteId"]),
    ],
)
data class MessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val conversationId: Long,
    val senderId: String,
    val body: String,
    val status: String,
    val sentAt: Long?,
    val isRead: Boolean,
    val remoteId: String? = null,
    @ColumnInfo(defaultValue = "'SYNCED'")
    val syncState: String = SyncStateValues.SYNCED,
)

object SyncStateValues {
    const val SYNCED = "SYNCED"
    const val PENDING = "PENDING"
    const val FAILED = "FAILED"
}
