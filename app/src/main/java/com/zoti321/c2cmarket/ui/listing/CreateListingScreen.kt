package com.zoti321.c2cmarket.ui.listing

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.zoti321.c2cmarket.R

private val CATEGORY_OPTIONS = listOf(
    "electronics" to R.string.category_electronics,
    "jewelery" to R.string.category_jewelery,
    "men's clothing" to R.string.category_mens_clothing,
    "women's clothing" to R.string.category_womens_clothing,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateListingScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CreateListingViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    var categoryExpanded by remember { mutableStateOf(false) }

    val pickImage = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri: Uri? ->
        uri?.let {
            try {
                context.contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION,
                )
            } catch (_: SecurityException) {
                // PickVisualMedia URI may not support persistable permission.
            }
            viewModel.updateImageUri(it.toString())
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                CreateListingEvent.Saved -> onBack()
                is CreateListingEvent.ValidationFailed -> {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.listing_create_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Button(
                onClick = {
                    pickImage.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.listing_pick_image))
            }
            if (uiState.imageUri.isNotBlank()) {
                AsyncImage(
                    model = uiState.imageUri,
                    contentDescription = null,
                    modifier = Modifier
                        .size(120.dp)
                        .padding(vertical = 8.dp),
                    contentScale = ContentScale.Crop,
                )
            }
            OutlinedTextField(
                value = uiState.title,
                onValueChange = viewModel::updateTitle,
                label = { Text(stringResource(R.string.listing_field_title)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                singleLine = true,
            )
            OutlinedTextField(
                value = uiState.price,
                onValueChange = viewModel::updatePrice,
                label = { Text(stringResource(R.string.listing_field_price)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                singleLine = true,
            )
            OutlinedTextField(
                value = uiState.description,
                onValueChange = viewModel::updateDescription,
                label = { Text(stringResource(R.string.listing_field_description)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                minLines = 3,
            )
            OutlinedTextField(
                value = uiState.meetupLocation,
                onValueChange = viewModel::updateMeetupLocation,
                label = { Text(stringResource(R.string.listing_field_meetup)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                singleLine = true,
            )
            ExposedDropdownMenuBox(
                expanded = categoryExpanded,
                onExpandedChange = { categoryExpanded = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
            ) {
                OutlinedTextField(
                    value = stringResource(
                        CATEGORY_OPTIONS.first { it.first == uiState.category }.second,
                    ),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.listing_field_category)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                )
                ExposedDropdownMenu(
                    expanded = categoryExpanded,
                    onDismissRequest = { categoryExpanded = false },
                ) {
                    CATEGORY_OPTIONS.forEach { (slug, labelRes) ->
                        DropdownMenuItem(
                            text = { Text(stringResource(labelRes)) },
                            onClick = {
                                viewModel.updateCategory(slug)
                                categoryExpanded = false
                            },
                        )
                    }
                }
            }
            Button(
                onClick = viewModel::submit,
                enabled = !uiState.isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
            ) {
                Text(stringResource(R.string.listing_submit))
            }
        }
    }
}
