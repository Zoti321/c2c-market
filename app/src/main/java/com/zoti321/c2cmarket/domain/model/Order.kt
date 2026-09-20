package com.zoti321.c2cmarket.domain.model

import java.time.Instant

data class OrderLineItem(
    val productId: Int,
    val title: String,
    val unitPrice: Double,
    val quantity: Int,
    val imageUrl: String,
)

data class Order(
    val id: Long,
    val guestId: String,
    val items: List<OrderLineItem>,
    val totalAmount: Double,
    val status: OrderStatus,
    val createdAt: Instant,
    val shipping: ShippingInfo? = null,
)

data class OrderSummary(
    val id: Long,
    val totalAmount: Double,
    val status: OrderStatus,
    val createdAt: Instant,
    val itemCount: Int,
)

fun Order.displayOrderNumber(): String = "#${id.toString().padStart(5, '0')}"

fun OrderSummary.displayOrderNumber(): String = "#${id.toString().padStart(5, '0')}"
