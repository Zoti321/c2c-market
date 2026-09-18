package com.zoti321.c2cmarket.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.zoti321.c2cmarket.domain.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    productRepository: ProductRepository,
) : ViewModel() {

    val productPagingFlow = productRepository
        .pagingProducts(pageSize = 10, sort = "desc")
        .cachedIn(viewModelScope)
}
