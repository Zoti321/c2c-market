package com.zoti321.c2cmarket.ui.order

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoti321.c2cmarket.domain.model.Order
import com.zoti321.c2cmarket.domain.repository.AuthRepository
import com.zoti321.c2cmarket.domain.repository.OrderRepository
import com.zoti321.c2cmarket.ui.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface OrderDetailUiState {
    data object Loading : OrderDetailUiState

    data class Success(
        val order: Order,
        val isBuyer: Boolean,
        val isSeller: Boolean,
    ) : OrderDetailUiState

    data object NotFound : OrderDetailUiState
}

sealed interface OrderDetailEvent {
    data class ActionFailed(val message: String) : OrderDetailEvent
}

@HiltViewModel
class OrderDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val orderRepository: OrderRepository,
    authRepository: AuthRepository,
) : ViewModel() {

    private val orderId: Long = checkNotNull(savedStateHandle[Routes.ORDER_ID_ARG])

    private val _events = MutableSharedFlow<OrderDetailEvent>()
    val events = _events.asSharedFlow()

    val uiState: StateFlow<OrderDetailUiState> = combine(
        orderRepository.observeOrder(orderId),
        authRepository.currentUserId(),
        orderRepository.observeOrdersAsSeller(),
    ) { order, userId, sellerOrders ->
        when {
            order == null -> OrderDetailUiState.NotFound
            else -> OrderDetailUiState.Success(
                order = order,
                isBuyer = order.guestId == userId,
                isSeller = sellerOrders.any { it.id == orderId },
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), OrderDetailUiState.Loading)

    fun confirmOrderAsSeller() = runAction { orderRepository.confirmOrderAsSeller(orderId) }

    fun confirmMeetupAsBuyer() = runAction { orderRepository.confirmMeetupAsBuyer(orderId) }

    fun confirmMeetupAsSeller() = runAction { orderRepository.confirmMeetupAsSeller(orderId) }

    fun cancelOrderAsSeller() = runAction { orderRepository.cancelOrderAsSeller(orderId) }

    private fun runAction(block: suspend () -> Result<Unit>) {
        viewModelScope.launch {
            block().onFailure { error ->
                _events.emit(OrderDetailEvent.ActionFailed(error.message ?: "操作失败"))
            }
        }
    }
}
