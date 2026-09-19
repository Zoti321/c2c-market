package com.zoti321.c2cmarket.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.zoti321.c2cmarket.domain.model.Product
import com.zoti321.c2cmarket.domain.repository.ListingRepository
import com.zoti321.c2cmarket.domain.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class HomeViewModel @Inject constructor(
    productRepository: ProductRepository,
    listingRepository: ListingRepository,
) : ViewModel() {

    val productPagingFlow = productRepository
        .pagingProducts(pageSize = 10, sort = "desc")
        .cachedIn(viewModelScope)

    val localListings: StateFlow<List<Product>> = listingRepository.observeAsProducts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
