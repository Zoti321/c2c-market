package com.zoti321.c2cmarket.domain.model

data class Product(
    val id: Int,
    val title: String,
    val price: Double,
    val description: String,
    val category: Category,
    val imageUrl: String,
    val rating: Rating,
)
