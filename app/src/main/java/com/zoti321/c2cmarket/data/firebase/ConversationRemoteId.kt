package com.zoti321.c2cmarket.data.firebase

fun buildConversationRemoteId(buyerId: String, sellerId: String, productId: Int): String =
    "${buyerId}_${sellerId}_$productId"
