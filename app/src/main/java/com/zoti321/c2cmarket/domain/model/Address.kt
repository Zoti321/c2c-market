package com.zoti321.c2cmarket.domain.model

import java.time.Instant

data class Address(
    val id: Long,
    val receiverName: String,
    val phone: String,
    val region: String,
    val detail: String,
    val isDefault: Boolean,
    val updatedAt: Instant,
) {
    fun formatted(): String = "$region $detail"

    fun toShippingInfo(): ShippingInfo = ShippingInfo(
        receiverName = receiverName,
        phone = phone,
        address = formatted(),
    )
}

data class AddressInput(
    val receiverName: String,
    val phone: String,
    val region: String,
    val detail: String,
    val isDefault: Boolean,
)
