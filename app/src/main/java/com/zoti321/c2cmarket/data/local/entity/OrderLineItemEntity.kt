package com.zoti321.c2cmarket.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "order_line_items",
    foreignKeys = [
        ForeignKey(
            entity = OrderEntity::class,
            parentColumns = ["id"],
            childColumns = ["orderId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("orderId")],
)
data class OrderLineItemEntity(
    @PrimaryKey(autoGenerate = true) val lineId: Long = 0,
    val orderId: Long,
    val productId: Int,
    val title: String,
    val unitPrice: Double,
    val quantity: Int,
    val imageUrl: String,
)
