package com.zoti321.c2cmarket.ui.product

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoti321.c2cmarket.domain.error.ProductNotFoundException
import com.zoti321.c2cmarket.domain.repository.CartRepository
import com.zoti321.c2cmarket.domain.repository.FavoriteRepository
import com.zoti321.c2cmarket.domain.repository.ProductRepository
import com.zoti321.c2cmarket.ui.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface ProductDetailEvent {
    data object AddedToCart : ProductDetailEvent

    data object ActionFailed : ProductDetailEvent
}

@HiltViewModel
class ProductDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val productRepository: ProductRepository,
    private val cartRepository: CartRepository,
    private val favoriteRepository: FavoriteRepository,
) : ViewModel() {

    private val productId: Int = checkNotNull(savedStateHandle[Routes.PRODUCT_ID_ARG])

    private val _uiState = MutableStateFlow<ProductDetailUiState>(ProductDetailUiState.Loading)
    val uiState: StateFlow<ProductDetailUiState> = _uiState.asStateFlow()

    val isFavorite: StateFlow<Boolean> = favoriteRepository.isFavorite(productId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    private val _events = MutableSharedFlow<ProductDetailEvent>()
    val events = _events.asSharedFlow()

    private val _isAddingToCart = MutableStateFlow(false)
    val isAddingToCart: StateFlow<Boolean> = _isAddingToCart.asStateFlow()

    init {
        loadProduct()
    }

    fun loadProduct() {
        viewModelScope.launch {
            _uiState.value = ProductDetailUiState.Loading
            _uiState.value = productRepository.getProduct(productId).fold(
                onSuccess = { ProductDetailUiState.Success(it) },
                onFailure = { error ->
                    ProductDetailUiState.Error(
                        when (error) {
                            is ProductNotFoundException -> ErrorType.NotFound
                            is IOException -> ErrorType.Network
                            else -> ErrorType.Generic
                        },
                    )
                },
            )
        }
    }

    fun addToCart() {
        val product = (_uiState.value as? ProductDetailUiState.Success)?.product ?: return
        viewModelScope.launch {
            _isAddingToCart.value = true
            val result = cartRepository.addItem(product)
            _isAddingToCart.value = false
            _events.emit(
                if (result.isSuccess) ProductDetailEvent.AddedToCart
                else ProductDetailEvent.ActionFailed,
            )
        }
    }

    fun toggleFavorite() {
        val product = (_uiState.value as? ProductDetailUiState.Success)?.product ?: return
        viewModelScope.launch {
            val result = favoriteRepository.toggleFavorite(product)
            if (result.isFailure) {
                _events.emit(ProductDetailEvent.ActionFailed)
            }
        }
    }
}
