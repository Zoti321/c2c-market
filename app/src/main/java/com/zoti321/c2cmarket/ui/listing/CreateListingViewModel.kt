package com.zoti321.c2cmarket.ui.listing

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoti321.c2cmarket.domain.model.ListingInput
import com.zoti321.c2cmarket.domain.repository.ListingRepository
import com.zoti321.c2cmarket.ui.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface CreateListingEvent {
    data object Saved : CreateListingEvent

    data class ValidationFailed(val message: String) : CreateListingEvent
}

data class CreateListingUiState(
    val title: String = "",
    val price: String = "",
    val description: String = "",
    val category: String = "electronics",
    val imageUri: String = "",
    val meetupLocation: String = "",
    val isSaving: Boolean = false,
)

@HiltViewModel
class CreateListingViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val listingRepository: ListingRepository,
) : ViewModel() {

    private val catalogId: Int? = savedStateHandle.get<Int>(Routes.LISTING_CATALOG_ID_ARG)
        ?.takeIf { it < 0 }

    private val _uiState = MutableStateFlow(CreateListingUiState())
    val uiState: StateFlow<CreateListingUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<CreateListingEvent>()
    val events = _events.asSharedFlow()

    init {
        catalogId?.let { id ->
            viewModelScope.launch {
                listingRepository.getProductByCatalogId(id).onSuccess { product ->
                    _uiState.value = CreateListingUiState(
                        title = product.title,
                        price = product.price.toString(),
                        description = product.description,
                        category = product.category,
                        imageUri = product.imageUrl,
                        meetupLocation = product.meetupLocation.orEmpty(),
                    )
                }
            }
        }
    }

    fun updateTitle(value: String) {
        _uiState.value = _uiState.value.copy(title = value)
    }

    fun updatePrice(value: String) {
        _uiState.value = _uiState.value.copy(price = value)
    }

    fun updateDescription(value: String) {
        _uiState.value = _uiState.value.copy(description = value)
    }

    fun updateCategory(value: String) {
        _uiState.value = _uiState.value.copy(category = value)
    }

    fun updateImageUri(uri: String) {
        _uiState.value = _uiState.value.copy(imageUri = uri)
    }

    fun updateMeetupLocation(value: String) {
        _uiState.value = _uiState.value.copy(meetupLocation = value)
    }

    fun submit() {
        val state = _uiState.value
        val price = state.price.toDoubleOrNull()
        if (price == null) {
            viewModelScope.launch {
                _events.emit(CreateListingEvent.ValidationFailed("请输入有效价格"))
            }
            return
        }
        val input = ListingInput(
            title = state.title,
            price = price,
            description = state.description,
            category = state.category,
            imageUri = state.imageUri,
            meetupLocation = state.meetupLocation.takeIf { it.isNotBlank() },
        )
        viewModelScope.launch {
            _uiState.value = state.copy(isSaving = true)
            val result = if (catalogId == null) {
                listingRepository.create(input)
            } else {
                listingRepository.update(catalogId, input)
            }
            _uiState.value = _uiState.value.copy(isSaving = false)
            result.fold(
                onSuccess = { _events.emit(CreateListingEvent.Saved) },
                onFailure = {
                    _events.emit(
                        CreateListingEvent.ValidationFailed(
                            it.message ?: "保存失败",
                        ),
                    )
                },
            )
        }
    }
}
