package com.zoti321.c2cmarket.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.zoti321.c2cmarket.R
import com.zoti321.c2cmarket.ui.common.EmptyContent
import com.zoti321.c2cmarket.ui.common.ErrorContent
import com.zoti321.c2cmarket.ui.common.userMessage
import com.zoti321.c2cmarket.ui.home.components.ProductCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onProductClick: (Int) -> Unit,
    onSearchClick: () -> Unit,
    modifier: Modifier = Modifier.testTag("home_screen"),
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val lazyPagingItems = viewModel.productPagingFlow.collectAsLazyPagingItems()
    val localListings by viewModel.localListings.collectAsStateWithLifecycle()
    val refreshState = lazyPagingItems.loadState.refresh
    val hasContent = localListings.isNotEmpty() || lazyPagingItems.itemCount > 0

    Scaffold(
        modifier = modifier,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.home_title)) },
                actions = {
                    IconButton(onClick = onSearchClick) {
                        Icon(Icons.Outlined.Search, contentDescription = stringResource(R.string.search_title))
                    }
                },
            )
        },
    ) { innerPadding ->
        when {
            refreshState is LoadState.Loading && !hasContent -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }

            refreshState is LoadState.Error && !hasContent -> {
                ErrorContent(
                    message = refreshState.error.userMessage(),
                    onRetry = { lazyPagingItems.retry() },
                    modifier = Modifier.padding(innerPadding),
                )
            }

            refreshState is LoadState.NotLoading && !hasContent -> {
                EmptyContent(
                    icon = Icons.Outlined.Inventory2,
                    message = stringResource(R.string.home_empty),
                    modifier = Modifier.padding(innerPadding),
                )
            }

            else -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    if (localListings.isNotEmpty()) {
                        item(span = { GridItemSpan(2) }) {
                            Text(
                                text = stringResource(R.string.home_local_listings),
                                style = MaterialTheme.typography.titleSmall,
                                modifier = Modifier.padding(bottom = 8.dp),
                            )
                        }
                        items(localListings.size) { index ->
                            val product = localListings[index]
                            ProductCard(
                                product = product,
                                onClick = { onProductClick(product.id) },
                            )
                        }
                    }

                    items(lazyPagingItems.itemCount) { index ->
                        val product = lazyPagingItems[index]
                        if (product != null) {
                            ProductCard(
                                product = product,
                                onClick = { onProductClick(product.id) },
                            )
                        }
                    }

                    if (lazyPagingItems.loadState.append is LoadState.Loading) {
                        item(span = { GridItemSpan(2) }) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.padding(8.dp),
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }
                        }
                    }

                    item(span = { GridItemSpan(2) }) {
                        Text(
                            text = stringResource(R.string.home_data_source),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                        )
                    }
                }
            }
        }
    }
}
