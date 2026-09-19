package com.zoti321.c2cmarket.ui.order

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoti321.c2cmarket.domain.repository.OrderRepository
import com.zoti321.c2cmarket.ui.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

sealed interface OrderDetailUiState {
    data object Loading : OrderDetailUiState

    data class Success(val order: com.zoti321.c2cmarket.domain.model.Order) : OrderDetailUiState

    data object NotFound : OrderDetailUiState
}

@HiltViewModel
class OrderDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    orderRepository: OrderRepository,
) : ViewModel() {

    private val orderId: Long = checkNotNull(savedStateHandle[Routes.ORDER_ID_ARG])

    val uiState: StateFlow<OrderDetailUiState> = orderRepository
        .observeOrder(orderId)
        .map { order ->
            when {
                order == null -> OrderDetailUiState.NotFound
                else -> OrderDetailUiState.Success(order)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), OrderDetailUiState.Loading)
}
