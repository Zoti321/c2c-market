package com.zoti321.c2cmarket.domain.model

enum class ConversationRole {
    BUYER,
    SELLER,
    ;

    companion object {
        fun fromRouteArg(arg: String?): ConversationRole =
            when (arg?.lowercase()) {
                "seller" -> SELLER
                else -> BUYER
            }
    }
}
