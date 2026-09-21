package com.zoti321.c2cmarket.ui.fake

import androidx.paging.PagingData
import com.zoti321.c2cmarket.data.repository.FakeListingRepository
import com.zoti321.c2cmarket.domain.model.BrowseHistoryItem
import com.zoti321.c2cmarket.domain.model.CartItem
import com.zoti321.c2cmarket.domain.model.Order
import com.zoti321.c2cmarket.domain.model.OrderSummary
import com.zoti321.c2cmarket.domain.repository.FavoriteRepository
import com.zoti321.c2cmarket.domain.model.Product
import com.zoti321.c2cmarket.domain.model.SearchResult
import com.zoti321.c2cmarket.domain.model.ShippingInfo
import com.zoti321.c2cmarket.data.repository.FakeAuthRepository
import com.zoti321.c2cmarket.data.repository.FakeChatRepository
import com.zoti321.c2cmarket.domain.repository.BrowseHistoryRepository
import com.zoti321.c2cmarket.domain.repository.CartRepository
import com.zoti321.c2cmarket.domain.repository.OrderRepository
import com.zoti321.c2cmarket.domain.repository.ProductRepository
import androidx.lifecycle.SavedStateHandle
import com.zoti321.c2cmarket.domain.GuestSession
import com.zoti321.c2cmarket.domain.model.Conversation
import com.zoti321.c2cmarket.domain.model.UserIds
import com.zoti321.c2cmarket.domain.model.UserProfile
import com.zoti321.c2cmarket.domain.model.OrderLineItem
import com.zoti321.c2cmarket.domain.model.OrderStatus
import com.zoti321.c2cmarket.ui.cart.CartViewModel
import com.zoti321.c2cmarket.ui.chat.ChatViewModel
import com.zoti321.c2cmarket.ui.chat.ConversationListViewModel
import com.zoti321.c2cmarket.ui.favorites.FavoriteListViewModel
import com.zoti321.c2cmarket.ui.home.HomeViewModel
import com.zoti321.c2cmarket.ui.order.OrderDetailViewModel
import java.time.Instant
import com.zoti321.c2cmarket.ui.navigation.Routes
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

private class FakeFavoriteRepository : FavoriteRepository {
    override fun isFavorite(productId: Int): Flow<Boolean> = flowOf(false)

    override fun observeFavorites(): Flow<List<Product>> = flowOf(emptyList())

    override suspend fun toggleFavorite(product: Product): Result<Boolean> = Result.success(false)

    override suspend fun removeFavorite(productId: Int): Result<Unit> = Result.success(Unit)
}

private class FakeOrderRepositoryWithOrder(
    private val order: Order,
) : OrderRepository {
    override suspend fun placeOrder(shipping: ShippingInfo): Result<Order> =
        Result.failure(UnsupportedOperationException())

    override fun observeOrders(): Flow<List<OrderSummary>> = flowOf(emptyList())

    override fun observeOrdersAsSeller(): Flow<List<OrderSummary>> = flowOf(
        listOf(
            OrderSummary(
                id = order.id,
                totalAmount = order.totalAmount,
                status = order.status,
                createdAt = order.createdAt,
                itemCount = order.items.sumOf { it.quantity },
            ),
        ),
    )

    override fun observeOrder(orderId: Long): Flow<Order?> =
        flowOf(if (orderId == order.id) order else null)

    override suspend fun isSellerForOrder(orderId: Long): Boolean = orderId == order.id

    override suspend fun confirmOrderAsSeller(orderId: Long): Result<Unit> = Result.success(Unit)

    override suspend fun confirmMeetupAsBuyer(orderId: Long): Result<Unit> = Result.success(Unit)

    override suspend fun confirmMeetupAsSeller(orderId: Long): Result<Unit> = Result.success(Unit)

    override suspend fun cancelOrderAsSeller(orderId: Long): Result<Unit> = Result.success(Unit)

    override suspend fun resolveLocalId(remoteOrderId: String): Long? = null

    override suspend fun retryPendingUploads() = Unit
}

private class EmptyOrderRepository : OrderRepository {
    override suspend fun placeOrder(shipping: ShippingInfo): Result<Order> =
        Result.failure(UnsupportedOperationException())

    override fun observeOrders(): Flow<List<OrderSummary>> = flowOf(emptyList())

    override fun observeOrdersAsSeller(): Flow<List<OrderSummary>> = flowOf(emptyList())

    override fun observeOrder(orderId: Long): Flow<Order?> = flowOf(null)

    override suspend fun isSellerForOrder(orderId: Long): Boolean = false

    override suspend fun confirmOrderAsSeller(orderId: Long): Result<Unit> = Result.success(Unit)

    override suspend fun confirmMeetupAsBuyer(orderId: Long): Result<Unit> = Result.success(Unit)

    override suspend fun confirmMeetupAsSeller(orderId: Long): Result<Unit> = Result.success(Unit)

    override suspend fun cancelOrderAsSeller(orderId: Long): Result<Unit> = Result.success(Unit)

    override suspend fun resolveLocalId(remoteOrderId: String): Long? = null

    override suspend fun retryPendingUploads() = Unit
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
        authRepository = FakeAuthRepository(),
    )

    fun signedInProfile(displayName: String = "测试用户"): ProfileViewModel = ProfileViewModel(
        orderRepository = EmptyOrderRepository(),
        browseHistoryRepository = EmptyBrowseHistoryRepository(),
        listingRepository = FakeListingRepository(),
        authRepository = FakeAuthRepository(
            initialUserId = UserIds.google("test-sub-123"),
            profile = UserProfile(
                userId = UserIds.google("test-sub-123"),
                displayName = displayName,
                email = "test@example.com",
                photoUrl = null,
            ),
        ),
    )

    fun conversationList(): ConversationListViewModel =
        ConversationListViewModel(
            SavedStateHandle(mapOf(Routes.CONVERSATION_ROLE_ARG to "buyer")),
            FakeChatRepository(),
        )

    fun sellerConversationList(): ConversationListViewModel =
        ConversationListViewModel(
            SavedStateHandle(mapOf(Routes.CONVERSATION_ROLE_ARG to "seller")),
            FakeChatRepository(),
        )

    fun favoriteList(): FavoriteListViewModel =
        FavoriteListViewModel(FakeFavoriteRepository())

    fun sellerOrderDetail(orderId: Long = 1L): OrderDetailViewModel {
        val order = Order(
            id = orderId,
            guestId = GuestSession.GUEST_ID,
            items = listOf(
                OrderLineItem(
                    productId = -1,
                    title = "Local Listing",
                    unitPrice = 50.0,
                    quantity = 1,
                    imageUrl = "content://img",
                ),
            ),
            totalAmount = 50.0,
            status = OrderStatus.PENDING,
            createdAt = Instant.ofEpochMilli(100L),
            meetupLocation = "深圳湾公园",
        )
        return OrderDetailViewModel(
            savedStateHandle = SavedStateHandle(mapOf(Routes.ORDER_ID_ARG to orderId)),
            orderRepository = FakeOrderRepositoryWithOrder(order),
            authRepository = FakeAuthRepository(initialUserId = UserIds.google("seller-1")),
        )
    }

    fun chat(conversationId: Long = 1L): ChatViewModel {
        val conversation = Conversation(
            id = conversationId,
            productId = 1,
            productTitle = "测试商品",
            productImageUrl = "https://example.com/img.jpg",
            sellerId = "seller-1",
            sellerDisplayName = "卖家",
            buyerId = GuestSession.GUEST_ID,
            buyerDisplayName = "游客",
            lastMessagePreview = "",
            lastMessageAt = 100L,
            unreadCount = 0,
            sellerUnreadCount = 0,
            createdAt = 100L,
        )
        return ChatViewModel(
            savedStateHandle = SavedStateHandle(
                mapOf(Routes.CONVERSATION_ID_ARG to conversationId),
            ),
            chatRepository = FakeChatRepository(
                conversations = listOf(conversation),
            ),
            authRepository = FakeAuthRepository(),
        )
    }
}
