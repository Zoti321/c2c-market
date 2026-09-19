package com.zoti321.c2cmarket.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "addresses")
data class AddressEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val receiverName: String,
    val phone: String,
    val region: String,
    val detail: String,
    val isDefault: Boolean,
    val updatedAt: Long,
)
