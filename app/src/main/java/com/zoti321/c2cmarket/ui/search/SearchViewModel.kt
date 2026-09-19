package com.zoti321.c2cmarket.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoti321.c2cmarket.domain.model.SearchResult
import com.zoti321.c2cmarket.domain.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.stateIn

@OptIn(FlowPreview::class, kotlinx.coroutines.ExperimentalCoroutinesApi::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val productRepository: ProductRepository,
) : ViewModel() {

    private val queryFlow = MutableStateFlow("")
    private val submitQuery = MutableSharedFlow<String>(extraBufferCapacity = 1)

    val searchResult: StateFlow<SearchResult> = merge(
        queryFlow.debounce(300).distinctUntilChanged(),
        submitQuery,
    )
        .flatMapLatest { query -> productRepository.searchProducts(query) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SearchResult.Idle)

    fun onQueryChange(query: String) {
        queryFlow.value = query
    }

    fun submitSearch(query: String) {
        queryFlow.value = query
        submitQuery.tryEmit(query)
    }

    fun retry() {
        submitSearch(queryFlow.value)
    }
}
