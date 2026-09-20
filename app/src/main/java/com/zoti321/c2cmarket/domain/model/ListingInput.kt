package com.zoti321.c2cmarket.domain.model

data class ListingInput(
    val title: String,
    val price: Double,
    val description: String,
    val category: String,
    val imageUri: String,
)
