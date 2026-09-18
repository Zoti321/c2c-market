package com.zoti321.c2cmarket.ui.common

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>

    data class Success<T>(val data: T) : UiState<T>

    data class Error(val throwable: Throwable) : UiState<Nothing>
}
