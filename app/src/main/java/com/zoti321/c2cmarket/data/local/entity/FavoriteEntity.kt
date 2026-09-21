package com.zoti321.c2cmarket.data.local.entity

import androidx.room.Entity

@Entity(
    tableName = "favorites",
    primaryKeys = ["userId", "productId"],
)
data class FavoriteEntity(
    val userId: String,
    val productId: Int,
    val createdAt: Long,
)
