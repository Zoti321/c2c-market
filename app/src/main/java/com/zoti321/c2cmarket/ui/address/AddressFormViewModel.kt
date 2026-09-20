package com.zoti321.c2cmarket.ui.address

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoti321.c2cmarket.domain.model.AddressInput
import com.zoti321.c2cmarket.domain.repository.AddressRepository
import com.zoti321.c2cmarket.ui.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface AddressFormEvent {
    data object Saved : AddressFormEvent

    data class Failed(val message: String) : AddressFormEvent
}

data class AddressFormUiState(
    val receiverName: String = "",
    val phone: String = "",
    val region: String = "",
    val detail: String = "",
    val isDefault: Boolean = true,
    val isSaving: Boolean = false,
)

@HiltViewModel
class AddressFormViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val addressRepository: AddressRepository,
) : ViewModel() {

    private val addressId: Long? = savedStateHandle.get<Long>(Routes.ADDRESS_ID_ARG)
        ?.takeIf { it > 0 }

    private val _uiState = MutableStateFlow(AddressFormUiState())
    val uiState: StateFlow<AddressFormUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<AddressFormEvent>()
    val events = _events.asSharedFlow()

    init {
        addressId?.let { id ->
            viewModelScope.launch {
                addressRepository.getById(id).onSuccess { address ->
                    _uiState.value = AddressFormUiState(
                        receiverName = address.receiverName,
                        phone = address.phone,
                        region = address.region,
                        detail = address.detail,
                        isDefault = address.isDefault,
                    )
                }
            }
        }
    }

    fun updateReceiverName(value: String) {
        _uiState.value = _uiState.value.copy(receiverName = value)
    }

    fun updatePhone(value: String) {
        _uiState.value = _uiState.value.copy(phone = value)
    }

    fun updateRegion(value: String) {
        _uiState.value = _uiState.value.copy(region = value)
    }

    fun updateDetail(value: String) {
        _uiState.value = _uiState.value.copy(detail = value)
    }

    fun updateIsDefault(value: Boolean) {
        _uiState.value = _uiState.value.copy(isDefault = value)
    }

    fun save() {
        val state = _uiState.value
        val input = AddressInput(
            receiverName = state.receiverName,
            phone = state.phone,
            region = state.region,
            detail = state.detail,
            isDefault = state.isDefault,
        )
        viewModelScope.launch {
            _uiState.value = state.copy(isSaving = true)
            val result = if (addressId == null) {
                addressRepository.create(input)
            } else {
                addressRepository.update(addressId, input)
            }
            _uiState.value = _uiState.value.copy(isSaving = false)
            result.fold(
                onSuccess = { _events.emit(AddressFormEvent.Saved) },
                onFailure = {
                    _events.emit(AddressFormEvent.Failed(it.message ?: "保存失败"))
                },
            )
        }
    }
}
