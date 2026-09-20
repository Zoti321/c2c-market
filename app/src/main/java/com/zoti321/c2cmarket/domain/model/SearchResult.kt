package com.zoti321.c2cmarket.domain.model

sealed interface SearchResult {
    data object Idle : SearchResult

    data object Loading : SearchResult

    data class Success(val products: List<Product>) : SearchResult

    data class Error(val throwable: Throwable) : SearchResult
}
