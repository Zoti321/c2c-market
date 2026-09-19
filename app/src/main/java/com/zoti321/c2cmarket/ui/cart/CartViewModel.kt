package com.zoti321.c2cmarket.ui.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoti321.c2cmarket.domain.model.CartItem
import com.zoti321.c2cmarket.domain.repository.CartRepository
import com.zoti321.c2cmarket.ui.common.UiState
import com.zoti321.c2cmarket.ui.common.asUiStateFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class CartViewModel @Inject constructor(
    private val cartRepository: CartRepository,
) : ViewModel() {

    private val retrySignal = MutableSharedFlow<Unit>(replay = 1).apply { tryEmit(Unit) }

    val uiState: StateFlow<UiState<List<CartItem>>> = retrySignal
        .flatMapLatest { cartRepository.observeItems().asUiStateFlow() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState.Loading)

    val totalPrice: StateFlow<Double> = cartRepository.observeItems()
        .map { list -> list.sumOf { it.unitPrice * it.quantity } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0.0)

    fun retry() {
        retrySignal.tryEmit(Unit)
    }

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
