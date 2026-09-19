package com.zoti321.c2cmarket.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "browse_history")
data class BrowseHistoryEntity(
    @PrimaryKey val productId: Int,
    val title: String,
    val imageUrl: String,
    val price: Double,
    val viewedAt: Long,
)
