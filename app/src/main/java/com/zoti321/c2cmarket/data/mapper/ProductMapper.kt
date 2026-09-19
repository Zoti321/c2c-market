package com.zoti321.c2cmarket.data.mapper

import com.zoti321.c2cmarket.data.remote.dto.ProductDto
import com.zoti321.c2cmarket.domain.model.Product
import com.zoti321.c2cmarket.domain.model.Rating

fun ProductDto.toDomain(): Product = Product(
    id = id,
    title = title,
    price = price,
    description = description,
    category = category,
    imageUrl = image,
    rating = Rating(rate = rating.rate, count = rating.count),
)
