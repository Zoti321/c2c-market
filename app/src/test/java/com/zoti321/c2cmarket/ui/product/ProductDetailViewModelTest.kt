package com.zoti321.c2cmarket.ui.product

import androidx.lifecycle.SavedStateHandle
import com.zoti321.c2cmarket.domain.model.Product
import com.zoti321.c2cmarket.domain.model.ProductSource
import com.zoti321.c2cmarket.domain.model.Rating
import com.zoti321.c2cmarket.data.repository.FakeAuthRepository
import com.zoti321.c2cmarket.domain.repository.BrowseHistoryRepository
import com.zoti321.c2cmarket.domain.repository.CartRepository
import com.zoti321.c2cmarket.domain.repository.ChatRepository
import com.zoti321.c2cmarket.domain.repository.FavoriteRepository
import com.zoti321.c2cmarket.domain.repository.ListingRepository
import com.zoti321.c2cmarket.domain.repository.ProductRepository
import com.zoti321.c2cmarket.ui.navigation.Routes
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProductDetailViewModelTest {

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loadProduct_negativeId_usesListingRepositoryAndRecordsHistory() = runTest {
        val listingProduct = sampleProduct(id = -1, source = ProductSource.LOCAL_LISTING)
        val browseHistory = RecordingBrowseHistoryRepository()
        val viewModel = ProductDetailViewModel(
            savedStateHandle = SavedStateHandle(mapOf(Routes.PRODUCT_ID_ARG to -1)),
            productRepositories = ProductDetailProductRepositories(
                productRepository = ThrowingProductRepository(),
                listingRepository = FixedListingRepository(listingProduct),
                cartRepository = NoOpCartRepository(),
                favoriteRepository = NoOpFavoriteRepository(),
                browseHistoryRepository = browseHistory,
            ),
            chatRepository = NoOpChatRepository(),
            authRepository = FakeAuthRepository(),
        )

        val state = viewModel.uiState.value
        assertTrue(state is ProductDetailUiState.Success)
        assertEquals(-1, (state as ProductDetailUiState.Success).product.id)
        assertEquals(1, browseHistory.recorded.size)
        assertEquals(-1, browseHistory.recorded.first().id)
    }

    private fun sampleProduct(id: Int, source: ProductSource) = Product(
        id = id,
        title = "Local Item",
        price = 10.0,
        description = "Desc",
        category = "electronics",
        imageUrl = "content://local",
        rating = Rating(0.0, 0),
        source = source,
    )
}

private class ThrowingProductRepository : ProductRepository {
    override suspend fun getProduct(id: Int) = error("should not call remote for negative id")

    override suspend fun getCategories() = error("unused")

    override suspend fun getProductsByCategory(category: String) = error("unused")

    override suspend fun getAllProducts() = error("unused")

    override fun pagingProducts(pageSize: Int, sort: String) = error("unused")

    override fun searchProducts(query: String): Flow<com.zoti321.c2cmarket.domain.model.SearchResult> =
        error("unused")
}

private class FixedListingRepository(
    private val product: Product,
) : ListingRepository {
    override fun observeAsProducts(): Flow<List<Product>> = flowOf(listOf(product))

    override fun observeByCategory(category: String): Flow<List<Product>> = flowOf(emptyList())

    override fun observeMyListings(): Flow<List<Product>> = flowOf(listOf(product))

    override suspend fun getProductByCatalogId(catalogId: Int): Result<Product> =
        if (catalogId == product.id) Result.success(product) else Result.failure(IllegalStateException())

    override suspend fun getSellerId(catalogId: Int): String? = "guest"

    override suspend fun create(input: com.zoti321.c2cmarket.domain.model.ListingInput) =
        error("unused")

    override suspend fun update(catalogId: Int, input: com.zoti321.c2cmarket.domain.model.ListingInput) =
        error("unused")

    override suspend fun delete(catalogId: Int) = error("unused")

    override suspend fun updateStatus(catalogId: Int, status: com.zoti321.c2cmarket.domain.model.ListingStatus) =
        Result.success(Unit)

    override suspend fun markReservedForCheckout(catalogIds: List<Int>) = Unit

    override suspend fun markSoldForOrder(orderId: Long) = Unit

    override suspend fun markAvailableForOrder(orderId: Long) = Unit

    override suspend fun searchLocal(query: String) = emptyList<Product>()
}

private class RecordingBrowseHistoryRepository : BrowseHistoryRepository {
    val recorded = mutableListOf<Product>()

    override fun observeRecent(): Flow<List<com.zoti321.c2cmarket.domain.model.BrowseHistoryItem>> =
        flowOf(emptyList())

    override suspend fun recordView(product: Product): Result<Unit> {
        recorded.add(product)
        return Result.success(Unit)
    }

    override suspend fun clearAll() = Result.success(Unit)
}

private class NoOpCartRepository : CartRepository {
    override fun observeItems(): Flow<List<com.zoti321.c2cmarket.domain.model.CartItem>> =
        flowOf(emptyList())

    override suspend fun addItem(product: Product) = Result.success(Unit)

    override suspend fun updateQuantity(productId: Int, quantity: Int) = Result.success(Unit)

    override suspend fun removeItem(productId: Int) = Result.success(Unit)

    override suspend fun clearAll() = Result.success(Unit)
}

private class NoOpFavoriteRepository : FavoriteRepository {
    override fun isFavorite(productId: Int): Flow<Boolean> = flowOf(false)

    override fun observeFavorites(): Flow<List<Product>> = flowOf(emptyList())

    override suspend fun toggleFavorite(product: Product) = Result.success(false)

    override suspend fun removeFavorite(productId: Int) = Result.success(Unit)
}

private class NoOpChatRepository : ChatRepository {
    override fun observeConversationsAsBuyer(): Flow<List<com.zoti321.c2cmarket.domain.model.Conversation>> =
        flowOf(emptyList())

    override fun observeConversationsAsSeller(): Flow<List<com.zoti321.c2cmarket.domain.model.Conversation>> =
        flowOf(emptyList())

    override fun observeConversationForCurrentUser(conversationId: Long): Flow<com.zoti321.c2cmarket.domain.model.Conversation?> =
        flowOf(null)

    override fun observeMessages(conversationId: Long): Flow<List<com.zoti321.c2cmarket.domain.model.Message>> =
        flowOf(emptyList())

    override suspend fun getOrCreateConversation(product: Product): Result<com.zoti321.c2cmarket.domain.model.Conversation> =
        Result.failure(IllegalStateException("unused"))

    override suspend fun getConversationForUser(conversationId: Long): com.zoti321.c2cmarket.domain.model.Conversation? = null

    override suspend fun saveDraft(conversationId: Long, body: String) = Unit

    override suspend fun sendMessage(conversationId: Long, body: String): Result<com.zoti321.c2cmarket.domain.model.Message> =
        Result.failure(IllegalStateException("unused"))

    override suspend fun markConversationRead(conversationId: Long) = Unit

    override fun observeDraft(conversationId: Long): Flow<String> = flowOf("")

    override suspend fun resolveLocalConversationId(remoteId: String): Long? = null

    override fun startActiveConversationSync(conversationId: Long) = Unit

    override fun stopActiveConversationSync() = Unit

    override suspend fun retryPendingUploads() = Unit
}
