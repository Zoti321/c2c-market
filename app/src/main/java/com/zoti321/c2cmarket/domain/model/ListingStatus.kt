package com.zoti321.c2cmarket.domain.model

enum class ListingStatus {
    AVAILABLE,
    RESERVED,
    SOLD,
    REMOVED,
    ;

    val isPubliclyVisible: Boolean
        get() = this == AVAILABLE
}
