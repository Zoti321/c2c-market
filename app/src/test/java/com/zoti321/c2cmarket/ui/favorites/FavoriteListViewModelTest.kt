package com.zoti321.c2cmarket.ui.favorites

import com.zoti321.c2cmarket.domain.model.Product
import com.zoti321.c2cmarket.domain.model.Rating
import com.zoti321.c2cmarket.domain.repository.FavoriteRepository
import com.zoti321.c2cmarket.ui.common.UiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FavoriteListViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun uiState_emitsFavoritesFromRepository() = runTest(dispatcher) {
        val product = sampleProduct()
        val repository = FakeFavoriteListRepository(initial = listOf(product))
        val viewModel = FavoriteListViewModel(repository)
        backgroundScope.launch { viewModel.uiState.collect {} }

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is UiState.Success)
        assertEquals(listOf(product.id), (state as UiState.Success).data.map { it.id })
    }

    @Test
    fun removeFavorite_updatesUiState() = runTest(dispatcher) {
        val product = sampleProduct()
        val repository = FakeFavoriteListRepository(initial = listOf(product))
        val viewModel = FavoriteListViewModel(repository)
        backgroundScope.launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        viewModel.removeFavorite(product.id)
        advanceUntilIdle()

        val state = viewModel.uiState.value as UiState.Success
        assertTrue(state.data.isEmpty())
    }

    private fun sampleProduct() = Product(
        id = 7,
        title = "Watch",
        price = 49.0,
        description = "Desc",
        category = "jewelery",
        imageUrl = "https://example.com/w.png",
        rating = Rating(3.5, 5),
    )
}

private class FakeFavoriteListRepository(
    initial: List<Product>,
) : FavoriteRepository {
    private val favorites = MutableStateFlow(initial)

    override fun isFavorite(productId: Int): Flow<Boolean> =
        favorites.map { list -> list.any { it.id == productId } }

    override fun observeFavorites(): Flow<List<Product>> = favorites.asStateFlow()

    override suspend fun toggleFavorite(product: Product): Result<Boolean> =
        Result.success(false)

    override suspend fun removeFavorite(productId: Int): Result<Unit> {
        favorites.value = favorites.value.filterNot { it.id == productId }
        return Result.success(Unit)
    }
}
