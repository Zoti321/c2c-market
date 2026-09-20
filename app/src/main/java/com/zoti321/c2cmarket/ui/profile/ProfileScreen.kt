package com.zoti321.c2cmarket.ui.profile

import android.app.Activity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.AssistChip
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.zoti321.c2cmarket.R
import com.zoti321.c2cmarket.domain.model.AuthState
import com.zoti321.c2cmarket.domain.model.BrowseHistoryItem
import com.zoti321.c2cmarket.domain.model.ListingStatus
import com.zoti321.c2cmarket.domain.model.OrderStatus
import com.zoti321.c2cmarket.domain.model.OrderSummary
import com.zoti321.c2cmarket.domain.model.Product
import com.zoti321.c2cmarket.domain.model.displayOrderNumber
import com.zoti321.c2cmarket.ui.common.EmptyContent
import com.zoti321.c2cmarket.ui.common.ErrorContent
import com.zoti321.c2cmarket.ui.common.LoadingContent
import com.zoti321.c2cmarket.ui.common.UiState
import com.zoti321.c2cmarket.ui.common.formatOrderDateTime
import com.zoti321.c2cmarket.ui.common.userMessage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    callbacks: ProfileScreenCallbacks,
    modifier: Modifier = Modifier.testTag("profile_screen"),
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val authState by viewModel.authState.collectAsStateWithLifecycle()
    val isSigningIn by viewModel.isSigningIn.collectAsStateWithLifecycle()
    val signInError by viewModel.signInError.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(signInError) {
        signInError?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearSignInError()
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.profile_title)) },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        when (val state = uiState) {
            is UiState.Loading -> LoadingContent(Modifier.padding(innerPadding))
            is UiState.Error -> ErrorContent(
                message = state.throwable.userMessage(),
                onRetry = viewModel::retry,
                modifier = Modifier.padding(innerPadding),
            )
            is UiState.Success -> {
                val data = state.data
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                ) {
                    item {
                        AuthSection(
                            authState = authState,
                            isSigningIn = isSigningIn,
                            onSignIn = {
                                val activity = context as? Activity
                                if (activity != null) {
                                    viewModel.signInWithGoogle(activity)
                                }
                            },
                            onSignOut = viewModel::signOut,
                        )
                    }
                    item {
                        ListItem(
                            modifier = Modifier.clickable(onClick = callbacks.onFavoritesClick),
                            headlineContent = { Text(stringResource(R.string.profile_favorites)) },
                            leadingContent = {
                                Icon(Icons.Outlined.FavoriteBorder, contentDescription = null)
                            },
                            trailingContent = {
                                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
                            },
                        )
                        HorizontalDivider()
                    }
                    item {
                        ListItem(
                            modifier = Modifier.clickable(onClick = callbacks.onBuyerMessagesClick),
                            headlineContent = { Text(stringResource(R.string.profile_messages)) },
                            leadingContent = {
                                Icon(Icons.Outlined.Chat, contentDescription = null)
                            },
                            trailingContent = {
                                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
                            },
                        )
                        HorizontalDivider()
                    }
                    item {
                        ListItem(
                            modifier = Modifier.clickable(onClick = callbacks.onSellerMessagesClick),
                            headlineContent = { Text(stringResource(R.string.profile_seller_messages)) },
                            leadingContent = {
                                Icon(Icons.Outlined.Chat, contentDescription = null)
                            },
                            trailingContent = {
                                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
                            },
                        )
                        HorizontalDivider()
                    }
                    item {
                        FilledTonalButton(
                            onClick = callbacks.onCreateListing,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                        ) {
                            Text(stringResource(R.string.listing_create_button))
                        }
                    }
                    if (data.myListings.isNotEmpty()) {
                        item {
                            Text(
                                text = stringResource(R.string.profile_my_listings),
                                style = MaterialTheme.typography.titleSmall,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            )
                        }
                        items(data.myListings, key = { it.id }) { listing ->
                            MyListingRow(
                                listing = listing,
                                onClick = { callbacks.onProductClick(listing.id) },
                                onEdit = { callbacks.onEditListing(listing.id) },
                                onDelete = { viewModel.deleteListing(listing.id) },
                                onMarkSold = { viewModel.updateListingStatus(listing.id, ListingStatus.SOLD) },
                                onMarkRemoved = { viewModel.updateListingStatus(listing.id, ListingStatus.REMOVED) },
                            )
                            HorizontalDivider()
                        }
                    }
                    item {
                        Text(
                            text = stringResource(R.string.profile_seller_orders_section),
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        )
                    }
                    if (data.sellerOrders.isEmpty()) {
                        item {
                            EmptyContent(
                                icon = Icons.Outlined.ReceiptLong,
                                message = stringResource(R.string.order_seller_empty),
                            )
                        }
                    } else {
                        items(data.sellerOrders, key = { "seller-${it.id}" }) { order ->
                            OrderCard(
                                order = order,
                                onClick = { callbacks.onOrderClick(order.id) },
                            )
                            HorizontalDivider()
                        }
                    }
                    item {
                        ListItem(
                            modifier = Modifier.clickable(onClick = callbacks.onManageAddresses),
                            headlineContent = { Text(stringResource(R.string.profile_addresses_entry)) },
                            leadingContent = {
                                Icon(Icons.Outlined.LocationOn, contentDescription = null)
                            },
                            trailingContent = {
                                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
                            },
                        )
                        HorizontalDivider()
                    }
                    if (data.browseHistory.isNotEmpty()) {
                        item {
                            BrowseHistoryHeader(onClear = viewModel::clearBrowseHistory)
                        }
                        item {
                            BrowseHistoryRow(
                                items = data.browseHistory,
                                onItemClick = callbacks.onProductClick,
                            )
                        }
                    }
                    item {
                        Text(
                            text = stringResource(R.string.profile_orders_section),
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        )
                    }
                    if (data.orders.isEmpty()) {
                        item {
                            EmptyContent(
                                icon = Icons.Outlined.ReceiptLong,
                                message = stringResource(R.string.order_empty),
                            )
                        }
                    } else {
                        items(data.orders, key = { it.id }) { order ->
                            OrderCard(
                                order = order,
                                onClick = { callbacks.onOrderClick(order.id) },
                            )
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AuthSection(
    authState: AuthState,
    isSigningIn: Boolean,
    onSignIn: () -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("profile_auth_section"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        when (authState) {
            AuthState.Guest -> {
                ListItem(
                    headlineContent = { Text(stringResource(R.string.profile_guest_mode)) },
                    leadingContent = {
                        Icon(Icons.Outlined.PersonOutline, contentDescription = null)
                    },
                )
                OutlinedButton(
                    onClick = onSignIn,
                    enabled = !isSigningIn,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 16.dp)
                        .testTag("profile_sign_in_button"),
                ) {
                    if (isSigningIn) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(18.dp)
                                .padding(end = 8.dp),
                            strokeWidth = 2.dp,
                        )
                    }
                    Text(stringResource(R.string.profile_sign_in_google))
                }
            }
            is AuthState.SignedIn -> {
                ListItem(
                    headlineContent = { Text(authState.profile.displayName) },
                    supportingContent = {
                        authState.profile.email?.let { Text(it) }
                    },
                    leadingContent = {
                        if (authState.profile.photoUrl != null) {
                            AsyncImage(
                                model = authState.profile.photoUrl,
                                contentDescription = authState.profile.displayName,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop,
                            )
                        } else {
                            Icon(Icons.Outlined.PersonOutline, contentDescription = null)
                        }
                    },
                    trailingContent = {
                        TextButton(onClick = onSignOut) {
                            Text(stringResource(R.string.profile_sign_out))
                        }
                    },
                )
            }
        }
    }
}

@Composable
fun GuestBanner(modifier: Modifier = Modifier) {
    AuthSection(
        authState = AuthState.Guest,
        isSigningIn = false,
        onSignIn = {},
        onSignOut = {},
        modifier = modifier,
    )
}

@Composable
private fun BrowseHistoryHeader(onClear: () -> Unit) {
    ListItem(
        headlineContent = { Text(stringResource(R.string.browse_history_title)) },
        trailingContent = {
            TextButton(onClick = onClear) {
                Text(stringResource(R.string.browse_history_clear))
            }
        },
    )
}

@Composable
private fun BrowseHistoryRow(
    items: List<BrowseHistoryItem>,
    onItemClick: (Int) -> Unit,
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(items, key = { it.productId }) { item ->
            Card(
                modifier = Modifier
                    .width(120.dp)
                    .clickable { onItemClick(item.productId) },
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    AsyncImage(
                        model = item.imageUrl,
                        contentDescription = item.title,
                        modifier = Modifier
                            .size(64.dp)
                            .align(Alignment.CenterHorizontally),
                        contentScale = ContentScale.Crop,
                    )
                    Text(
                        text = item.title,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Text(
                        text = stringResource(R.string.price_format, item.price),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.tertiary,
                    )
                }
            }
        }
    }
}

@Composable
private fun MyListingRow(
    listing: Product,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onMarkSold: () -> Unit,
    onMarkRemoved: () -> Unit,
) {
    val status = listing.listingStatus
    ListItem(
        modifier = Modifier.clickable(onClick = onClick),
        headlineContent = { Text(listing.title, maxLines = 1) },
        supportingContent = {
            Column {
                Text(stringResource(R.string.price_format, listing.price))
                if (status != null) {
                    AssistChip(
                        onClick = {},
                        label = { Text(stringResource(listingStatusLabel(status))) },
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
                if (status == ListingStatus.AVAILABLE || status == ListingStatus.RESERVED) {
                    Row(
                        modifier = Modifier.padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        TextButton(onClick = onMarkSold) {
                            Text(stringResource(R.string.listing_mark_sold))
                        }
                        TextButton(onClick = onMarkRemoved) {
                            Text(stringResource(R.string.listing_mark_removed))
                        }
                    }
                }
            }
        },
        trailingContent = {
            IconButton(onClick = onEdit) {
                Icon(Icons.Outlined.Edit, contentDescription = stringResource(R.string.listing_edit))
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Outlined.Delete, contentDescription = stringResource(R.string.cart_delete))
            }
        },
    )
}

@Composable
private fun listingStatusLabel(status: ListingStatus): Int = when (status) {
    ListingStatus.AVAILABLE -> R.string.listing_status_available
    ListingStatus.RESERVED -> R.string.listing_status_reserved
    ListingStatus.SOLD -> R.string.listing_status_sold
    ListingStatus.REMOVED -> R.string.listing_status_removed
}

@Composable
private fun OrderCard(
    order: OrderSummary,
    onClick: () -> Unit,
) {
    ListItem(
        modifier = Modifier.clickable(onClick = onClick),
        headlineContent = {
            Text(stringResource(R.string.order_number, order.displayOrderNumber()))
        },
        supportingContent = {
            Text(
                text = "${order.createdAt.formatOrderDateTime()} · " +
                    stringResource(orderStatusLabel(order.status)),
            )
            Text(
                text = stringResource(R.string.price_format, order.totalAmount) + " · " +
                    stringResource(R.string.order_item_count, order.itemCount),
                color = MaterialTheme.colorScheme.tertiary,
            )
        },
        trailingContent = {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
        },
    )
}

@Composable
private fun orderStatusLabel(status: OrderStatus): Int = when (status) {
    OrderStatus.PENDING -> R.string.order_status_pending
    OrderStatus.CONFIRMED -> R.string.order_status_confirmed
    OrderStatus.COMPLETED -> R.string.order_status_completed
    OrderStatus.CANCELLED -> R.string.order_status_cancelled
}
