package com.zoti321.c2cmarket.ui.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoti321.c2cmarket.domain.model.CartItem
import com.zoti321.c2cmarket.domain.repository.CartRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class CartViewModel @Inject constructor(
    private val cartRepository: CartRepository,
) : ViewModel() {

    val items: StateFlow<List<CartItem>> = cartRepository.observeItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val totalPrice: StateFlow<Double> = items
        .map { list -> list.sumOf { it.unitPrice * it.quantity } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0.0)

    val isEmpty: StateFlow<Boolean> = items
        .map { it.isEmpty() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    fun increment(productId: Int, currentQty: Int) {
        viewModelScope.launch {
            cartRepository.updateQuantity(productId, currentQty + 1)
        }
    }

    fun decrement(productId: Int, currentQty: Int) {
        if (currentQty <= 1) return
        viewModelScope.launch {
            cartRepository.updateQuantity(productId, currentQty - 1)
        }
    }

    fun removeItem(productId: Int) {
        viewModelScope.launch {
            cartRepository.removeItem(productId)
        }
    }
}
