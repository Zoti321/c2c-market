package com.zoti321.c2cmarket.data.mapper

import com.zoti321.c2cmarket.data.local.entity.BrowseHistoryEntity
import com.zoti321.c2cmarket.domain.model.BrowseHistoryItem
import com.zoti321.c2cmarket.domain.model.Product
import java.time.Instant

fun Product.toBrowseHistoryEntity(viewedAt: Long): BrowseHistoryEntity = BrowseHistoryEntity(
    productId = id,
    title = title,
    imageUrl = imageUrl,
    price = price,
    viewedAt = viewedAt,
)

fun BrowseHistoryEntity.toDomain(): BrowseHistoryItem = BrowseHistoryItem(
    productId = productId,
    title = title,
    imageUrl = imageUrl,
    price = price,
    viewedAt = Instant.ofEpochMilli(viewedAt),
)
