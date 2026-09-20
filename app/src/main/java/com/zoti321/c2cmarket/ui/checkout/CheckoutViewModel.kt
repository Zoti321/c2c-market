package com.zoti321.c2cmarket.ui.checkout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoti321.c2cmarket.domain.model.Address
import com.zoti321.c2cmarket.domain.model.CartItem
import com.zoti321.c2cmarket.domain.model.AuthState
import com.zoti321.c2cmarket.domain.repository.AddressRepository
import com.zoti321.c2cmarket.domain.repository.AuthRepository
import com.zoti321.c2cmarket.domain.repository.CartRepository
import com.zoti321.c2cmarket.domain.repository.OrderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface CheckoutEvent {
    data class OrderPlaced(val orderId: Long) : CheckoutEvent

    data object PlaceOrderFailed : CheckoutEvent
}

@HiltViewModel
class CheckoutViewModel @Inject constructor(
    cartRepository: CartRepository,
    addressRepository: AddressRepository,
    authRepository: AuthRepository,
    private val orderRepository: OrderRepository,
) : ViewModel() {

    val items: StateFlow<List<CartItem>> = cartRepository.observeItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val totalPrice: StateFlow<Double> = items
        .map { list -> list.sumOf { it.unitPrice * it.quantity } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0.0)

    val isEmpty: StateFlow<Boolean> = items
        .map { it.isEmpty() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    val addresses: StateFlow<List<Address>> = addressRepository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _selectedAddressId = MutableStateFlow<Long?>(null)
    val selectedAddressId: StateFlow<Long?> = _selectedAddressId.asStateFlow()

    val selectedAddress: StateFlow<Address?> = combine(
        addresses,
        selectedAddressId,
    ) { list, selectedId ->
        selectedId?.let { id -> list.firstOrNull { it.id == id } }
            ?: list.firstOrNull { it.isDefault }
            ?: list.firstOrNull()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val canPlaceOrder: StateFlow<Boolean> = selectedAddress
        .map { it != null }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val authState: StateFlow<AuthState> = authRepository.observeAuthState()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AuthState.Guest)

    private val _isPlacingOrder = MutableStateFlow(false)
    val isPlacingOrder: StateFlow<Boolean> = _isPlacingOrder.asStateFlow()

    private val _events = MutableSharedFlow<CheckoutEvent>()
    val events = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            addressRepository.observeDefault().collect { default ->
                if (_selectedAddressId.value == null && default != null) {
                    _selectedAddressId.value = default.id
                }
            }
        }
    }

    fun selectAddress(id: Long) {
        _selectedAddressId.value = id
    }

    fun placeOrder() {
        val address = selectedAddress.value ?: return
        viewModelScope.launch {
            _isPlacingOrder.value = true
            val result = orderRepository.placeOrder(address.toShippingInfo())
            _isPlacingOrder.value = false
            if (result.isSuccess) {
                _events.emit(CheckoutEvent.OrderPlaced(result.getOrThrow().id))
            } else {
                _events.emit(CheckoutEvent.PlaceOrderFailed)
            }
        }
    }
}
