package com.zoti321.c2cmarket.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoti321.c2cmarket.domain.model.BrowseHistoryItem
import com.zoti321.c2cmarket.domain.model.OrderSummary
import com.zoti321.c2cmarket.domain.model.Product
import com.zoti321.c2cmarket.domain.repository.BrowseHistoryRepository
import com.zoti321.c2cmarket.domain.repository.ListingRepository
import com.zoti321.c2cmarket.domain.repository.OrderRepository
import com.zoti321.c2cmarket.ui.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ProfileUiData(
    val orders: List<OrderSummary>,
    val browseHistory: List<BrowseHistoryItem>,
    val myListings: List<Product>,
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    orderRepository: OrderRepository,
    private val browseHistoryRepository: BrowseHistoryRepository,
    private val listingRepository: ListingRepository,
) : ViewModel() {

    private val retrySignal = MutableSharedFlow<Unit>(replay = 1).apply { tryEmit(Unit) }

    val uiState: StateFlow<UiState<ProfileUiData>> = retrySignal
        .flatMapLatest {
            combine(
                orderRepository.observeOrders(),
                browseHistoryRepository.observeRecent(),
                listingRepository.observeMyListings(),
            ) { orders, history, listings ->
                UiState.Success(
                    ProfileUiData(
                        orders = orders,
                        browseHistory = history,
                        myListings = listings,
                    ),
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState.Loading)

    fun retry() {
        retrySignal.tryEmit(Unit)
    }

    fun clearBrowseHistory() {
        viewModelScope.launch {
            browseHistoryRepository.clearAll()
        }
    }

    fun deleteListing(catalogId: Int) {
        viewModelScope.launch {
            listingRepository.delete(catalogId)
        }
    }
}
