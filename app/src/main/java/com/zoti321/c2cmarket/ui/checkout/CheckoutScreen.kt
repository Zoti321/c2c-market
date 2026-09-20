package com.zoti321.c2cmarket.ui.checkout

import android.Manifest
import android.os.Build
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.zoti321.c2cmarket.R
import com.zoti321.c2cmarket.domain.model.Address
import com.zoti321.c2cmarket.domain.model.CartItem
import com.zoti321.c2cmarket.ui.address.AddressPickerBottomSheet
import com.zoti321.c2cmarket.ui.common.ErrorContent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    onBack: () -> Unit,
    onOrderPlaced: (Long) -> Unit,
    onAddAddress: () -> Unit,
    onManageAddresses: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CheckoutViewModel = hiltViewModel(),
) {
    val items by viewModel.items.collectAsStateWithLifecycle()
    val totalPrice by viewModel.totalPrice.collectAsStateWithLifecycle()
    val isEmpty by viewModel.isEmpty.collectAsStateWithLifecycle()
    val isPlacingOrder by viewModel.isPlacingOrder.collectAsStateWithLifecycle()
    val selectedAddress by viewModel.selectedAddress.collectAsStateWithLifecycle()
    val selectedAddressId by viewModel.selectedAddressId.collectAsStateWithLifecycle()
    val addresses by viewModel.addresses.collectAsStateWithLifecycle()
    val canPlaceOrder by viewModel.canPlaceOrder.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val checkoutFailedMessage = stringResource(R.string.checkout_failed)
    var showAddressPicker by remember { mutableStateOf(false) }

    val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission(),
    ) { _ ->
        viewModel.placeOrder()
    }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is CheckoutEvent.OrderPlaced -> onOrderPlaced(event.orderId)
                CheckoutEvent.PlaceOrderFailed -> {
                    snackbarHostState.showSnackbar(checkoutFailedMessage)
                }
            }
        }
    }

    if (showAddressPicker) {
        AddressPickerBottomSheet(
            addresses = addresses,
            selectedAddressId = selectedAddressId,
            onSelect = {
                viewModel.selectAddress(it)
                showAddressPicker = false
            },
            onManageAddresses = {
                showAddressPicker = false
                onManageAddresses()
            },
            onDismiss = { showAddressPicker = false },
        )
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.checkout_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        if (isEmpty) {
            ErrorContent(
                message = stringResource(R.string.checkout_empty_cart),
                onBack = onBack,
                modifier = Modifier.padding(innerPadding),
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            ) {
                ShippingAddressCard(
                    address = selectedAddress,
                    onClick = {
                        if (addresses.isEmpty()) {
                            onAddAddress()
                        } else {
                            showAddressPicker = true
                        }
                    },
                )
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(items, key = { it.productId }) { item ->
                        CheckoutItemRow(item)
                        HorizontalDivider()
                    }
                }
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(R.string.cart_total, totalPrice),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        text = stringResource(R.string.checkout_guest_hint),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp),
                    )
                    Button(
                        onClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                viewModel.placeOrder()
                            }
                        },
                        enabled = !isPlacingOrder && canPlaceOrder,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiary,
                        ),
                    ) {
                        Text(stringResource(R.string.checkout_confirm))
                    }
                }
            }
        }
    }
}

@Composable
private fun ShippingAddressCard(
    address: Address?,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable(onClick = onClick),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.checkout_shipping_title),
                style = MaterialTheme.typography.titleSmall,
            )
            if (address == null) {
                Text(
                    text = stringResource(R.string.checkout_add_address),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp),
                )
            } else {
                Text(
                    text = "${address.receiverName}  ${address.phone}",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp),
                )
                Text(
                    text = address.formatted(),
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun CheckoutItemRow(item: CartItem) {
    ListItem(
        leadingContent = {
            AsyncImage(
                model = item.imageUrl,
                contentDescription = item.title,
                modifier = Modifier.size(48.dp),
                contentScale = ContentScale.Crop,
            )
        },
        headlineContent = { Text(item.title, maxLines = 1) },
        trailingContent = {
            Text(
                text = stringResource(R.string.quantity_format, item.quantity) + "  " +
                    stringResource(R.string.price_format, item.unitPrice * item.quantity),
            )
        },
    )
}
