package com.zoti321.c2cmarket.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ProductDto(
    val id: Int = 0,
    val title: String = "",
    val price: Double = 0.0,
    val description: String = "",
    val category: String = "",
    val image: String = "",
    val rating: RatingDto = RatingDto(),
)

@Serializable
data class RatingDto(
    val rate: Double = 0.0,
    val count: Int = 0,
)
