package com.zoti321.c2cmarket.data.mapper

import com.zoti321.c2cmarket.data.local.entity.ListingEntity
import com.zoti321.c2cmarket.domain.model.ListingInput
import com.zoti321.c2cmarket.domain.model.ListingStatus
import com.zoti321.c2cmarket.domain.model.Product
import com.zoti321.c2cmarket.domain.model.ProductSource
import com.zoti321.c2cmarket.domain.model.Rating

fun ListingEntity.toProduct(): Product = Product(
    id = catalogId,
    title = title,
    price = price,
    description = description,
    category = category,
    imageUrl = imageUri,
    rating = Rating(rate = 0.0, count = 0),
    source = ProductSource.LOCAL_LISTING,
    meetupLocation = meetupLocation,
    listingStatus = ListingStatus.valueOf(status),
)

fun ListingInput.toEntity(catalogId: Int, now: Long, sellerId: String): ListingEntity = ListingEntity(
    catalogId = catalogId,
    title = title.trim(),
    price = price,
    description = description.trim(),
    category = category,
    imageUri = imageUri,
    sellerId = sellerId,
    meetupLocation = meetupLocation?.trim()?.takeIf { it.isNotEmpty() },
    status = ListingStatus.AVAILABLE.name,
    createdAt = now,
    updatedAt = now,
)
