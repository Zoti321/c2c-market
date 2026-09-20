package com.zoti321.c2cmarket.ui.order

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AssistChip
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.zoti321.c2cmarket.R
import com.zoti321.c2cmarket.domain.model.displayOrderNumber
import com.zoti321.c2cmarket.domain.model.OrderLineItem
import com.zoti321.c2cmarket.ui.common.ErrorContent
import com.zoti321.c2cmarket.ui.common.GeoMapIntents
import com.zoti321.c2cmarket.ui.common.LoadingContent
import com.zoti321.c2cmarket.ui.common.formatOrderDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: OrderDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val mapOpenFailedMessage = stringResource(R.string.map_open_failed)

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.order_detail_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                },
            )
        },
    ) { innerPadding ->
        when (val state = uiState) {
            is OrderDetailUiState.Loading -> LoadingContent(Modifier.padding(innerPadding))
            is OrderDetailUiState.NotFound -> ErrorContent(
                message = stringResource(R.string.order_not_found),
                onBack = onBack,
                modifier = Modifier.padding(innerPadding),
            )
            is OrderDetailUiState.Success -> {
                val order = state.order
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = stringResource(R.string.order_number, order.displayOrderNumber()),
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text(
                            text = order.createdAt.formatOrderDateTime(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        AssistChip(
                            onClick = {},
                            label = { Text(stringResource(R.string.order_status_completed)) },
                            modifier = Modifier.padding(top = 8.dp),
                        )
                    }
                    if (order.shipping != null) {
                        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                            Text(
                                text = stringResource(R.string.order_shipping_title),
                                style = MaterialTheme.typography.titleSmall,
                            )
                            Text(
                                text = "${order.shipping.receiverName}  ${order.shipping.phone}",
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            Text(
                                text = order.shipping.address,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            TextButton(
                                onClick = {
                                    val opened = GeoMapIntents.open(context, order.shipping.address)
                                    if (!opened) {
                                        scope.launch {
                                            snackbarHostState.showSnackbar(mapOpenFailedMessage)
                                        }
                                    }
                                },
                            ) {
                                Text(stringResource(R.string.map_view_on_map))
                            }
                        }
                        HorizontalDivider()
                    } else {
                        Text(
                            text = stringResource(R.string.order_shipping_missing),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        )
                        HorizontalDivider()
                    }
                    LazyColumn(modifier = Modifier.weight(1f)) {
                        items(order.items, key = { it.productId }) { item ->
                            OrderLineItemRow(item)
                            HorizontalDivider()
                        }
                    }
                    Text(
                        text = stringResource(R.string.order_total, order.totalAmount),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(16.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun OrderLineItemRow(item: OrderLineItem) {
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
