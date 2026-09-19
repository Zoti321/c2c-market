package com.zoti321.c2cmarket.domain.model

import java.time.Instant

data class BrowseHistoryItem(
    val productId: Int,
    val title: String,
    val imageUrl: String,
    val price: Double,
    val viewedAt: Instant,
)
