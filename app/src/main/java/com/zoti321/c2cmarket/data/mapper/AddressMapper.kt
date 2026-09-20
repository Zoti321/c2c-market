package com.zoti321.c2cmarket.data.mapper

import com.zoti321.c2cmarket.data.local.entity.AddressEntity
import com.zoti321.c2cmarket.domain.model.Address
import com.zoti321.c2cmarket.domain.model.AddressInput
import java.time.Instant

fun AddressEntity.toDomain(): Address = Address(
    id = id,
    receiverName = receiverName,
    phone = phone,
    region = region,
    detail = detail,
    isDefault = isDefault,
    updatedAt = Instant.ofEpochMilli(updatedAt),
)

fun AddressInput.toEntity(id: Long = 0, updatedAt: Long): AddressEntity = AddressEntity(
    id = id,
    receiverName = receiverName.trim(),
    phone = phone.trim(),
    region = region.trim(),
    detail = detail.trim(),
    isDefault = isDefault,
    updatedAt = updatedAt,
)
