package com.zoti321.c2cmarket.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "listings",
    indices = [Index(value = ["catalogId"], unique = true)],
)
data class ListingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val catalogId: Int,
    val title: String,
    val price: Double,
    val description: String,
    val category: String,
    val imageUri: String,
    val createdAt: Long,
    val updatedAt: Long,
)
