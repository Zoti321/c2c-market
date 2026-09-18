package com.zoti321.c2cmarket.ui.checkout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoti321.c2cmarket.domain.model.CartItem
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

    private val _isPlacingOrder = MutableStateFlow(false)
    val isPlacingOrder: StateFlow<Boolean> = _isPlacingOrder.asStateFlow()

    private val _events = MutableSharedFlow<CheckoutEvent>()
    val events = _events.asSharedFlow()

    fun placeOrder() {
        viewModelScope.launch {
            _isPlacingOrder.value = true
            val result = orderRepository.placeOrder()
            _isPlacingOrder.value = false
            if (result.isSuccess) {
                _events.emit(CheckoutEvent.OrderPlaced(result.getOrThrow().id))
            } else {
                _events.emit(CheckoutEvent.PlaceOrderFailed)
            }
        }
    }
}
