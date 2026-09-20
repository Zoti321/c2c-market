package com.zoti321.c2cmarket.ui.category

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoti321.c2cmarket.domain.model.Product
import com.zoti321.c2cmarket.domain.repository.ListingRepository
import com.zoti321.c2cmarket.domain.repository.ProductRepository
import com.zoti321.c2cmarket.ui.common.UiState
import com.zoti321.c2cmarket.ui.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@HiltViewModel
class CategoryProductsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val productRepository: ProductRepository,
    private val listingRepository: ListingRepository,
) : ViewModel() {

    val categorySlug: String = checkNotNull(savedStateHandle[Routes.CATEGORY_SLUG_ARG])

    private val _uiState = MutableStateFlow<UiState<List<Product>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<Product>>> = _uiState.asStateFlow()

    init {
        loadProducts()
    }

    fun loadProducts() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            val remoteResult = productRepository.getProductsByCategory(categorySlug)
            val localListings = listingRepository.observeByCategory(categorySlug).first()
            _uiState.value = remoteResult.fold(
                onSuccess = { remote ->
                    UiState.Success(localListings + remote)
                },
                onFailure = { error ->
                    if (localListings.isNotEmpty()) {
                        UiState.Success(localListings)
                    } else {
                        UiState.Error(error)
                    }
                },
            )
        }
    }
}
