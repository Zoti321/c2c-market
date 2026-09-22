package com.zoti321.c2cmarket.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "orders",
    indices = [Index(value = ["remoteId"])],
)
data class OrderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val guestId: String,
    val totalAmount: Double,
    val status: String,
    val createdAt: Long,
    val shippingReceiverName: String? = null,
    val shippingPhone: String? = null,
    val shippingAddress: String? = null,
    val meetupLocation: String? = null,
    @ColumnInfo(defaultValue = "0")
    val buyerMeetupConfirmed: Boolean = false,
    @ColumnInfo(defaultValue = "0")
    val sellerMeetupConfirmed: Boolean = false,
    val remoteId: String? = null,
    @ColumnInfo(defaultValue = "'SYNCED'")
    val syncState: String = SyncStateValues.SYNCED,
)
