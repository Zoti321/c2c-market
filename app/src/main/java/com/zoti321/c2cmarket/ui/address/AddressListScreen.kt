package com.zoti321.c2cmarket.ui.address

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zoti321.c2cmarket.R
import com.zoti321.c2cmarket.domain.model.Address
import com.zoti321.c2cmarket.ui.common.EmptyContent
import com.zoti321.c2cmarket.ui.common.ErrorContent
import com.zoti321.c2cmarket.ui.common.LoadingContent
import com.zoti321.c2cmarket.ui.common.UiState
import com.zoti321.c2cmarket.ui.common.userMessage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddressListScreen(
    onBack: () -> Unit,
    onCreateAddress: () -> Unit,
    onEditAddress: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AddressListViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var deleteTarget by remember { mutableStateOf<Address?>(null) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.address_list_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onCreateAddress) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.address_add))
            }
        },
    ) { innerPadding ->
        when (val state = uiState) {
            is UiState.Loading -> LoadingContent(Modifier.padding(innerPadding))
            is UiState.Error -> ErrorContent(
                message = state.throwable.userMessage(),
                onRetry = viewModel::retry,
                modifier = Modifier.padding(innerPadding),
            )
            is UiState.Success -> {
                if (state.data.isEmpty()) {
                    EmptyContent(
                        icon = Icons.Outlined.LocationOn,
                        message = stringResource(R.string.address_empty),
                        actionLabel = stringResource(R.string.address_add),
                        onAction = onCreateAddress,
                        modifier = Modifier.padding(innerPadding),
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                    ) {
                        items(state.data, key = { it.id }) { address ->
                            AddressRow(
                                address = address,
                                onClick = { onEditAddress(address.id) },
                                onDelete = { deleteTarget = address },
                                onSetDefault = { viewModel.setDefault(address.id) },
                            )
                        }
                    }
                }
            }
        }
    }

    deleteTarget?.let { address ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text(stringResource(R.string.address_delete_confirm)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteAddress(address.id)
                        deleteTarget = null
                    },
                ) {
                    Text(stringResource(R.string.cart_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) {
                    Text(stringResource(R.string.action_back))
                }
            },
        )
    }
}

@Composable
private fun AddressRow(
    address: Address,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onSetDefault: () -> Unit,
) {
    ListItem(
        modifier = Modifier.clickable(onClick = onClick),
        headlineContent = {
            if (address.isDefault) {
                AssistChip(onClick = {}, label = { Text(stringResource(R.string.address_default_badge)) })
            }
            Text("${address.receiverName}  ${address.phone}")
        },
        supportingContent = { Text(address.formatted()) },
        trailingContent = {
            if (!address.isDefault) {
                TextButton(onClick = onSetDefault) {
                    Text(stringResource(R.string.address_set_default))
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Outlined.Delete, contentDescription = stringResource(R.string.cart_delete))
            }
        },
    )
}
