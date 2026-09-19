package com.zoti321.c2cmarket.ui.product

import com.zoti321.c2cmarket.domain.model.Product

sealed interface ProductDetailUiState {
    data object Loading : ProductDetailUiState

    data class Success(val product: Product) : ProductDetailUiState

    data class Error(val type: ErrorType) : ProductDetailUiState
}

enum class ErrorType {
    Network,
    Generic,
    NotFound,
}
