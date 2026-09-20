package com.zoti321.c2cmarket.ui.common

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

fun Instant.formatOrderDateTime(): String {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        .withZone(ZoneId.systemDefault())
    return formatter.format(this)
}

fun Instant.formatRelativeTime(): String {
    val now = Instant.now()
    val minutes = java.time.Duration.between(this, now).toMinutes()
    return when {
        minutes < 1 -> "刚刚"
        minutes < 60 -> "${minutes}分钟前"
        minutes < 24 * 60 -> "${minutes / 60}小时前"
        else -> formatOrderDateTime()
    }
}

fun formatPrice(amount: Double): String = "$%.2f".format(amount)
