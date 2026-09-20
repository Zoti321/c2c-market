package com.zoti321.c2cmarket.ui.address

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.zoti321.c2cmarket.R
import com.zoti321.c2cmarket.domain.model.Address

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddressPickerBottomSheet(
    addresses: List<Address>,
    selectedAddressId: Long?,
    onSelect: (Long) -> Unit,
    onManageAddresses: () -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(modifier = Modifier.padding(bottom = 16.dp)) {
            Text(
                text = stringResource(R.string.checkout_pick_address),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
            LazyColumn {
                items(addresses, key = { it.id }) { address ->
                    ListItem(
                        modifier = Modifier.clickable { onSelect(address.id) },
                        headlineContent = { Text("${address.receiverName}  ${address.phone}") },
                        supportingContent = { Text(address.formatted()) },
                        trailingContent = {
                            if (address.id == selectedAddressId) {
                                Icon(Icons.Default.Check, contentDescription = null)
                            }
                        },
                    )
                    HorizontalDivider()
                }
            }
            TextButton(
                onClick = onManageAddresses,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            ) {
                Text(stringResource(R.string.checkout_manage_addresses))
            }
        }
    }
}
