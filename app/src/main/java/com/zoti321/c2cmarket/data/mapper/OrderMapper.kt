package com.zoti321.c2cmarket.data.mapper

import com.zoti321.c2cmarket.data.local.entity.OrderEntity
import com.zoti321.c2cmarket.data.local.entity.OrderLineItemEntity
import com.zoti321.c2cmarket.domain.model.Order
import com.zoti321.c2cmarket.domain.model.OrderLineItem
import com.zoti321.c2cmarket.domain.model.OrderStatus
import com.zoti321.c2cmarket.domain.model.OrderSummary
import com.zoti321.c2cmarket.domain.model.ShippingInfo
import java.time.Instant

fun OrderLineItemEntity.toDomain(): OrderLineItem = OrderLineItem(
    productId = productId,
    title = title,
    unitPrice = unitPrice,
    quantity = quantity,
    imageUrl = imageUrl,
)

fun OrderEntity.toSummary(itemCount: Int): OrderSummary = OrderSummary(
    id = id,
    totalAmount = totalAmount,
    status = OrderStatus.valueOf(status),
    createdAt = Instant.ofEpochMilli(createdAt),
    itemCount = itemCount,
)

fun OrderEntity.toDomain(lineItems: List<OrderLineItemEntity>): Order {
    val shipping = if (
        shippingReceiverName != null &&
        shippingPhone != null &&
        shippingAddress != null
    ) {
        ShippingInfo(
            receiverName = shippingReceiverName,
            phone = shippingPhone,
            address = shippingAddress,
        )
    } else {
        null
    }
    return Order(
        id = id,
        guestId = guestId,
        items = lineItems.map { it.toDomain() },
        totalAmount = totalAmount,
        status = OrderStatus.valueOf(status),
        createdAt = Instant.ofEpochMilli(createdAt),
        shipping = shipping,
    )
}
