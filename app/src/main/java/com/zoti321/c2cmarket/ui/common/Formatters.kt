package com.zoti321.c2cmarket.ui.common

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

fun Instant.formatOrderDateTime(): String {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        .withZone(ZoneId.systemDefault())
    return formatter.format(this)
}

fun formatPrice(amount: Double): String = "$%.2f".format(amount)
