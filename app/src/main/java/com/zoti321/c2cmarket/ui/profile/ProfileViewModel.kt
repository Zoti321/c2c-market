package com.zoti321.c2cmarket.ui.profile

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoti321.c2cmarket.domain.model.AuthState
import com.zoti321.c2cmarket.domain.model.BrowseHistoryItem
import com.zoti321.c2cmarket.domain.model.ListingStatus
import com.zoti321.c2cmarket.domain.model.OrderSummary
import com.zoti321.c2cmarket.domain.model.Product
import com.zoti321.c2cmarket.domain.repository.AuthRepository
import com.zoti321.c2cmarket.domain.repository.BrowseHistoryRepository
import com.zoti321.c2cmarket.domain.repository.ListingRepository
import com.zoti321.c2cmarket.domain.repository.OrderRepository
import com.zoti321.c2cmarket.ui.common.UiState
import com.zoti321.c2cmarket.ui.common.fallbackUserMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ProfileUiData(
    val orders: List<OrderSummary>,
    val sellerOrders: List<OrderSummary>,
    val browseHistory: List<BrowseHistoryItem>,
    val myListings: List<Product>,
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    orderRepository: OrderRepository,
    private val browseHistoryRepository: BrowseHistoryRepository,
    private val listingRepository: ListingRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val retrySignal = MutableSharedFlow<Unit>(replay = 1).apply { tryEmit(Unit) }

    val authState: StateFlow<AuthState> = authRepository.observeAuthState()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AuthState.Guest)

    private val _isSigningIn = MutableStateFlow(false)
    val isSigningIn: StateFlow<Boolean> = _isSigningIn.asStateFlow()

    private val _signInError = MutableStateFlow<String?>(null)
    val signInError: StateFlow<String?> = _signInError.asStateFlow()

    val uiState: StateFlow<UiState<ProfileUiData>> = retrySignal
        .flatMapLatest {
            combine(
                orderRepository.observeOrders(),
                orderRepository.observeOrdersAsSeller(),
                browseHistoryRepository.observeRecent(),
                listingRepository.observeMyListings(),
            ) { orders, sellerOrders, history, listings ->
                UiState.Success(
                    ProfileUiData(
                        orders = orders,
                        sellerOrders = sellerOrders,
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

    fun updateListingStatus(catalogId: Int, status: ListingStatus) {
        viewModelScope.launch {
            listingRepository.updateStatus(catalogId, status)
        }
    }

    fun signInWithGoogle(activity: Activity) {
        viewModelScope.launch {
            _isSigningIn.value = true
            _signInError.value = null
            val result = authRepository.signInWithGoogle(activity)
            _isSigningIn.value = false
            if (result.isFailure) {
                _signInError.value = result.exceptionOrNull()?.fallbackUserMessage()
                    ?: "登录失败，请重试"
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
        }
    }

    fun clearSignInError() {
        _signInError.value = null
    }
}
