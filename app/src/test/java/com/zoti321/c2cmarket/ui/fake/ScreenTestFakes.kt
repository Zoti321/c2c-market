package com.zoti321.c2cmarket.ui.fake

import androidx.paging.PagingData
import com.zoti321.c2cmarket.data.repository.FakeListingRepository
import com.zoti321.c2cmarket.domain.model.BrowseHistoryItem
import com.zoti321.c2cmarket.domain.model.CartItem
import com.zoti321.c2cmarket.domain.model.Order
import com.zoti321.c2cmarket.domain.model.OrderSummary
import com.zoti321.c2cmarket.domain.model.Product
import com.zoti321.c2cmarket.domain.model.SearchResult
import com.zoti321.c2cmarket.domain.model.ShippingInfo
import com.zoti321.c2cmarket.domain.repository.BrowseHistoryRepository
import com.zoti321.c2cmarket.domain.repository.CartRepository
import com.zoti321.c2cmarket.domain.repository.OrderRepository
import com.zoti321.c2cmarket.domain.repository.ProductRepository
import com.zoti321.c2cmarket.ui.cart.CartViewModel
import com.zoti321.c2cmarket.ui.home.HomeViewModel
import com.zoti321.c2cmarket.ui.profile.ProfileViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

private class EmptyProductRepository : ProductRepository {
    override fun pagingProducts(pageSize: Int, sort: String): Flow<PagingData<Product>> =
        flowOf(PagingData.empty())

    override suspend fun getProduct(id: Int): Result<Product> =
        Result.failure(IllegalStateException("unused"))

    override suspend fun getCategories(): Result<List<String>> = Result.success(emptyList())

    override suspend fun getProductsByCategory(category: String): Result<List<Product>> =
        Result.success(emptyList())

    override suspend fun getAllProducts(): Result<List<Product>> = Result.success(emptyList())

    override fun searchProducts(query: String): Flow<SearchResult> = flowOf(SearchResult.Idle)
}

private class EmptyCartRepository : CartRepository {
    override fun observeItems(): Flow<List<CartItem>> = flowOf(emptyList())

    override suspend fun addItem(product: Product): Result<Unit> = Result.success(Unit)

    override suspend fun updateQuantity(productId: Int, quantity: Int): Result<Unit> = Result.success(Unit)

    override suspend fun removeItem(productId: Int): Result<Unit> = Result.success(Unit)

    override suspend fun clearAll(): Result<Unit> = Result.success(Unit)
}

private class EmptyOrderRepository : OrderRepository {
    override suspend fun placeOrder(shipping: ShippingInfo): Result<Order> =
        Result.failure(UnsupportedOperationException())

    override fun observeOrders(): Flow<List<OrderSummary>> = flowOf(emptyList())

    override fun observeOrder(orderId: Long): Flow<Order?> = flowOf(null)
}

private class EmptyBrowseHistoryRepository : BrowseHistoryRepository {
    override fun observeRecent(): Flow<List<BrowseHistoryItem>> = flowOf(emptyList())

    override suspend fun recordView(product: Product): Result<Unit> = Result.success(Unit)

    override suspend fun clearAll(): Result<Unit> = Result.success(Unit)
}

object ScreenTestViewModels {
    fun home(): HomeViewModel = HomeViewModel(
        productRepository = EmptyProductRepository(),
        listingRepository = FakeListingRepository(),
    )

    fun emptyCart(): CartViewModel = CartViewModel(
        cartRepository = EmptyCartRepository(),
    )

    fun guestProfile(): ProfileViewModel = ProfileViewModel(
        orderRepository = EmptyOrderRepository(),
        browseHistoryRepository = EmptyBrowseHistoryRepository(),
        listingRepository = FakeListingRepository(),
    )
}
