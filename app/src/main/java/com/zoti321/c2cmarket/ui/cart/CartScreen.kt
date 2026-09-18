package com.zoti321.c2cmarket.ui.cart

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.zoti321.c2cmarket.R
import com.zoti321.c2cmarket.domain.model.CartItem
import com.zoti321.c2cmarket.ui.common.EmptyContent
import com.zoti321.c2cmarket.ui.theme.DestructiveRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    onCheckout: () -> Unit,
    onGoHome: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CartViewModel = hiltViewModel(),
) {
    val items by viewModel.items.collectAsStateWithLifecycle()
    val totalPrice by viewModel.totalPrice.collectAsStateWithLifecycle()
    val isEmpty by viewModel.isEmpty.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.cart_title)) },
            )
        },
        bottomBar = {
            if (!isEmpty) {
                CartBottomBar(
                    totalPrice = totalPrice,
                    onCheckout = onCheckout,
                )
            }
        },
    ) { innerPadding ->
        if (isEmpty) {
            EmptyContent(
                icon = Icons.Outlined.ShoppingCart,
                message = stringResource(R.string.cart_empty),
                actionLabel = stringResource(R.string.cart_go_home),
                onAction = onGoHome,
                modifier = Modifier.padding(innerPadding),
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            ) {
                items(items, key = { it.productId }) { item ->
                    CartItemRow(
                        item = item,
                        onIncrement = { viewModel.increment(item.productId, item.quantity) },
                        onDecrement = { viewModel.decrement(item.productId, item.quantity) },
                        onRemove = { viewModel.removeItem(item.productId) },
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun CartItemRow(
    item: CartItem,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onRemove: () -> Unit,
) {
    ListItem(
        leadingContent = {
            AsyncImage(
                model = item.imageUrl,
                contentDescription = item.title,
                modifier = Modifier.size(64.dp),
                contentScale = ContentScale.Crop,
            )
        },
        headlineContent = {
            Text(item.title, maxLines = 2)
        },
        supportingContent = {
            Text(
                text = stringResource(R.string.price_format, item.unitPrice),
                color = MaterialTheme.colorScheme.tertiary,
            )
        },
        trailingContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onDecrement,
                    enabled = item.quantity > 1,
                ) {
                    Icon(Icons.Outlined.Remove, contentDescription = null)
                }
                Text(text = item.quantity.toString())
                FilledIconButton(onClick = onIncrement) {
                    Text("+")
                }
                IconButton(onClick = onRemove) {
                    Icon(
                        Icons.Outlined.DeleteOutline,
                        contentDescription = stringResource(R.string.cart_delete),
                        tint = DestructiveRed,
                    )
                }
            }
        },
    )
}

@Composable
private fun CartBottomBar(
    totalPrice: Double,
    onCheckout: () -> Unit,
) {
    Surface(tonalElevation = 3.dp) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Text(
                text = stringResource(R.string.cart_total, totalPrice),
                style = MaterialTheme.typography.titleMedium,
            )
            Button(
                onClick = onCheckout,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.tertiary,
                ),
            ) {
                Text(stringResource(R.string.cart_checkout))
            }
        }
    }
}
