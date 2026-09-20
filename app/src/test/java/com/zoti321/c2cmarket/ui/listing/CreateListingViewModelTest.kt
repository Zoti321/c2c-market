package com.zoti321.c2cmarket.ui.listing

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.zoti321.c2cmarket.domain.model.ListingInput
import com.zoti321.c2cmarket.domain.model.Product
import com.zoti321.c2cmarket.domain.model.ProductSource
import com.zoti321.c2cmarket.domain.model.Rating
import com.zoti321.c2cmarket.domain.repository.ListingRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CreateListingViewModelTest {

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun submit_invalidPrice_emitsValidationFailed() = runTest {
        val viewModel = CreateListingViewModel(
            savedStateHandle = SavedStateHandle(),
            listingRepository = SuccessfulListingRepository(),
        )
        viewModel.updatePrice("not-a-number")

        viewModel.events.test {
            viewModel.submit()
            assertEquals(CreateListingEvent.ValidationFailed("请输入有效价格"), awaitItem())
        }
    }

    @Test
    fun submit_validInput_emitsSaved() = runTest {
        val viewModel = CreateListingViewModel(
            savedStateHandle = SavedStateHandle(),
            listingRepository = SuccessfulListingRepository(),
        )
        viewModel.updateTitle("Phone")
        viewModel.updatePrice("9.99")
        viewModel.updateDescription("Desc")
        viewModel.updateImageUri("content://test/image")

        viewModel.events.test {
            viewModel.submit()
            assertEquals(CreateListingEvent.Saved, awaitItem())
        }
    }
}

private class SuccessfulListingRepository : ListingRepository {
    override fun observeAsProducts(): Flow<List<Product>> = flowOf(emptyList())

    override fun observeByCategory(category: String): Flow<List<Product>> = flowOf(emptyList())

    override fun observeMyListings(): Flow<List<Product>> = flowOf(emptyList())

    override suspend fun getProductByCatalogId(catalogId: Int): Result<Product> =
        Result.failure(IllegalStateException())

    override suspend fun getSellerId(catalogId: Int): String? = "guest"

    override suspend fun create(input: ListingInput): Result<Product> = Result.success(
        Product(
            id = -1,
            title = input.title,
            price = input.price,
            description = input.description,
            category = input.category,
            imageUrl = input.imageUri,
            rating = Rating(0.0, 0),
            source = ProductSource.LOCAL_LISTING,
        ),
    )

    override suspend fun update(catalogId: Int, input: ListingInput): Result<Product> =
        create(input)

    override suspend fun delete(catalogId: Int) = Result.success(Unit)

    override suspend fun searchLocal(query: String) = emptyList<Product>()
}
