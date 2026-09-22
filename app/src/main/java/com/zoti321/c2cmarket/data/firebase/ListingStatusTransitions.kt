package com.zoti321.c2cmarket.data.firebase

import com.zoti321.c2cmarket.domain.model.ListingStatus

object ListingStatusTransitions {
    private val allowed = mapOf(
        ListingStatus.AVAILABLE to setOf(ListingStatus.RESERVED, ListingStatus.REMOVED),
        ListingStatus.RESERVED to setOf(ListingStatus.AVAILABLE, ListingStatus.SOLD),
        ListingStatus.SOLD to emptySet(),
        ListingStatus.REMOVED to emptySet(),
    )

    fun canTransition(from: ListingStatus, to: ListingStatus): Boolean =
        from == to || allowed[from]?.contains(to) == true
}
