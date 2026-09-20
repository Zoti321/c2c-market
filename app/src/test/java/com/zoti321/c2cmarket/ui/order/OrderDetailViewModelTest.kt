package com.zoti321.c2cmarket.ui.order

import androidx.lifecycle.SavedStateHandle
import com.zoti321.c2cmarket.data.repository.FakeAuthRepository
import com.zoti321.c2cmarket.domain.GuestSession
import com.zoti321.c2cmarket.domain.model.Order
import com.zoti321.c2cmarket.domain.model.OrderLineItem
import com.zoti321.c2cmarket.domain.model.OrderStatus
import com.zoti321.c2cmarket.domain.model.OrderSummary
import com.zoti321.c2cmarket.domain.model.ShippingInfo
import com.zoti321.c2cmarket.domain.model.UserIds
import com.zoti321.c2cmarket.domain.repository.OrderRepository
import com.zoti321.c2cmarket.ui.navigation.Routes
import java.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
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
class OrderDetailViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private val orderId = 42L

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun uiState_sellerRole_usesIsSellerForOrder() = runTest(dispatcher) {
        val order = sampleOrder(orderId)
        val repository = FakeOrderDetailRepository(
            order = order,
            isSeller = true,
        )
        val viewModel = OrderDetailViewModel(
            SavedStateHandle(mapOf(Routes.ORDER_ID_ARG to orderId)),
            repository,
            FakeAuthRepository(initialUserId = UserIds.google("seller-1")),
        )
        backgroundScope.launch { viewModel.uiState.collect {} }

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is OrderDetailUiState.Success)
        val success = state as OrderDetailUiState.Success
        assertTrue(success.isSeller)
        assertEquals(false, success.isBuyer)
    }

    @Test
    fun uiState_buyerRole_marksBuyerNotSeller() = runTest(dispatcher) {
        val order = sampleOrder(orderId)
        val viewModel = OrderDetailViewModel(
            SavedStateHandle(mapOf(Routes.ORDER_ID_ARG to orderId)),
            FakeOrderDetailRepository(order = order, isSeller = false),
            FakeAuthRepository(),
        )
        backgroundScope.launch { viewModel.uiState.collect {} }

        advanceUntilIdle()

        val state = viewModel.uiState.value as OrderDetailUiState.Success
        assertTrue(state.isBuyer)
        assertEquals(false, state.isSeller)
    }

    private fun sampleOrder(id: Long) = Order(
        id = id,
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
}

private class FakeOrderDetailRepository(
    private val order: Order,
    private val isSeller: Boolean,
) : OrderRepository {
    override suspend fun placeOrder(shipping: ShippingInfo): Result<Order> =
        Result.failure(UnsupportedOperationException())

    override fun observeOrders(): Flow<List<OrderSummary>> = flowOf(emptyList())

    override fun observeOrdersAsSeller(): Flow<List<OrderSummary>> = flowOf(emptyList())

    override fun observeOrder(orderId: Long): Flow<Order?> =
        flowOf(if (orderId == order.id) order else null)

    override suspend fun isSellerForOrder(orderId: Long): Boolean =
        orderId == order.id && isSeller

    override suspend fun confirmOrderAsSeller(orderId: Long): Result<Unit> = Result.success(Unit)

    override suspend fun confirmMeetupAsBuyer(orderId: Long): Result<Unit> = Result.success(Unit)

    override suspend fun confirmMeetupAsSeller(orderId: Long): Result<Unit> = Result.success(Unit)

    override suspend fun cancelOrderAsSeller(orderId: Long): Result<Unit> = Result.success(Unit)
}
