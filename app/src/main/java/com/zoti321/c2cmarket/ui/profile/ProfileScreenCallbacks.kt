package com.zoti321.c2cmarket.ui.profile

data class ProfileScreenCallbacks(
    val onOrderClick: (Long) -> Unit,
    val onProductClick: (Int) -> Unit,
    val onCreateListing: () -> Unit,
    val onEditListing: (Int) -> Unit,
    val onManageAddresses: () -> Unit,
    val onMessagesClick: () -> Unit,
)
