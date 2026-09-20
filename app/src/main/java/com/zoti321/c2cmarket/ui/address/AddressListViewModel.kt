package com.zoti321.c2cmarket.ui.address

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoti321.c2cmarket.domain.model.Address
import com.zoti321.c2cmarket.domain.repository.AddressRepository
import com.zoti321.c2cmarket.ui.common.UiState
import com.zoti321.c2cmarket.ui.common.asUiStateFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class AddressListViewModel @Inject constructor(
    private val addressRepository: AddressRepository,
) : ViewModel() {

    private val refreshSignal = MutableSharedFlow<Unit>(replay = 1).apply { tryEmit(Unit) }

    val uiState: StateFlow<UiState<List<Address>>> = refreshSignal
        .flatMapLatest { addressRepository.observeAll().asUiStateFlow() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState.Loading)

    fun retry() {
        refreshSignal.tryEmit(Unit)
    }

    fun deleteAddress(id: Long) {
        viewModelScope.launch {
            addressRepository.delete(id)
        }
    }

    fun setDefault(id: Long) {
        viewModelScope.launch {
            addressRepository.setDefault(id)
        }
    }
}
