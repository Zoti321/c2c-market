package com.zoti321.c2cmarket.ui.product

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zoti321.c2cmarket.R
import com.zoti321.c2cmarket.domain.model.ProductSource
import com.zoti321.c2cmarket.ui.category.categoryDisplayName
import com.zoti321.c2cmarket.ui.common.ErrorContent
import com.zoti321.c2cmarket.ui.common.LoadingContent
import com.zoti321.c2cmarket.ui.product.components.ProductDetailBottomBar
import com.zoti321.c2cmarket.ui.product.components.ProductHeroImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    onBack: () -> Unit,
    onEditListing: (Int) -> Unit,
    onContactSeller: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProductDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isFavorite by viewModel.isFavorite.collectAsStateWithLifecycle()
    val isAddingToCart by viewModel.isAddingToCart.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val addedToCartMessage = stringResource(R.string.product_added_to_cart)
    val actionFailedMessage = stringResource(R.string.product_action_failed)

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                ProductDetailEvent.AddedToCart -> {
                    snackbarHostState.showSnackbar(addedToCartMessage)
                }
                ProductDetailEvent.ActionFailed -> {
                    snackbarHostState.showSnackbar(actionFailedMessage)
                }
                ProductDetailEvent.Deleted -> onBack()
            }
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.product_detail_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                },
                actions = {
                    if (viewModel.isLocalListing()) {
                        val productId = (uiState as? ProductDetailUiState.Success)?.product?.id
                        if (productId != null) {
                            IconButton(onClick = { onEditListing(productId) }) {
                                Icon(Icons.Outlined.Edit, contentDescription = stringResource(R.string.listing_edit))
                            }
                            IconButton(onClick = viewModel::deleteListing) {
                                Icon(Icons.Outlined.Delete, contentDescription = stringResource(R.string.cart_delete))
                            }
                        }
                    }
                },
            )
        },
        bottomBar = {
            if (uiState is ProductDetailUiState.Success) {
                ProductDetailBottomBar(
                    isFavorite = isFavorite,
                    isAddingToCart = isAddingToCart,
                    onToggleFavorite = viewModel::toggleFavorite,
                    onAddToCart = viewModel::addToCart,
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        when (val state = uiState) {
            is ProductDetailUiState.Loading -> LoadingContent(Modifier.padding(innerPadding))
            is ProductDetailUiState.Error -> {
                val message = when (state.type) {
                    ErrorType.NotFound -> stringResource(R.string.product_not_found)
                    ErrorType.Network -> stringResource(R.string.error_network)
                    ErrorType.Generic -> stringResource(R.string.error_generic)
                }
                ErrorContent(
                    message = message,
                    onRetry = if (state.type != ErrorType.NotFound) viewModel::loadProduct else null,
                    onBack = onBack,
                    modifier = Modifier.padding(innerPadding),
                )
            }
            is ProductDetailUiState.Success -> {
                val product = state.product
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .verticalScroll(rememberScrollState()),
                ) {
                    ProductHeroImage(
                        imageUrl = product.imageUrl,
                        contentDescription = product.title,
                    )
                    Text(
                        text = stringResource(R.string.price_format, product.price),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                    Row(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Text(
                            text = product.title,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.weight(1f),
                        )
                        if (product.source == ProductSource.LOCAL_LISTING) {
                            AssistChip(
                                onClick = {},
                                label = { Text(stringResource(R.string.listing_chip)) },
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        AssistChip(
                            onClick = {},
                            label = { Text(categoryDisplayName(product.category)) },
                        )
                        if (product.source != ProductSource.LOCAL_LISTING) {
                            Text(
                                text = stringResource(
                                    R.string.rating_format,
                                    product.rating.rate,
                                    product.rating.count,
                                ),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 4.dp),
                            )
                        }
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                    Text(
                        text = product.description,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                    if (product.source != ProductSource.LOCAL_LISTING) {
                        OutlinedButton(
                            onClick = { viewModel.contactSeller(onContactSeller) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                        ) {
                            Text(stringResource(R.string.contact_seller))
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}
