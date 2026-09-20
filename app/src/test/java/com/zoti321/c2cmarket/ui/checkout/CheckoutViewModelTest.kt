package com.zoti321.c2cmarket.ui.checkout

import app.cash.turbine.test
import com.zoti321.c2cmarket.domain.model.Address
import com.zoti321.c2cmarket.domain.model.CartItem
import com.zoti321.c2cmarket.domain.model.Order
import com.zoti321.c2cmarket.domain.model.OrderStatus
import com.zoti321.c2cmarket.domain.model.OrderSummary
import com.zoti321.c2cmarket.domain.model.ShippingInfo
import com.zoti321.c2cmarket.data.repository.FakeAuthRepository
import com.zoti321.c2cmarket.domain.repository.AddressRepository
import com.zoti321.c2cmarket.domain.repository.CartRepository
import com.zoti321.c2cmarket.domain.repository.OrderRepository
import java.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CheckoutViewModelTest {

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
    fun canPlaceOrder_false_whenNoAddress() = runTest(dispatcher) {
        val viewModel = CheckoutViewModel(
            cartRepository = FakeCartRepository(),
            addressRepository = FakeAddressRepository(emptyList()),
            authRepository = FakeAuthRepository(),
            orderRepository = FakeOrderRepository(),
        )
        assertFalse(viewModel.canPlaceOrder.value)
    }

    @Test
    fun selectAddress_updatesSelectedAddressId() = runTest(dispatcher) {
        val address = sampleAddress()
        val viewModel = CheckoutViewModel(
            cartRepository = FakeCartRepository(),
            addressRepository = FakeAddressRepository(listOf(address)),
            authRepository = FakeAuthRepository(),
            orderRepository = FakeOrderRepository(),
        )
        viewModel.selectAddress(address.id)
        assertEquals(address.id, viewModel.selectedAddressId.value)
    }

    @Test
    fun placeOrder_withAddress_emitsOrderPlaced() = runTest(dispatcher) {
        val address = sampleAddress()
        val viewModel = CheckoutViewModel(
            cartRepository = FakeCartRepository(),
            addressRepository = FakeAddressRepository(listOf(address)),
            authRepository = FakeAuthRepository(),
            orderRepository = FakeOrderRepository(),
        )
        backgroundScope.launch { viewModel.selectedAddress.collect { } }

        viewModel.events.test {
            viewModel.placeOrder()
            assertEquals(CheckoutEvent.OrderPlaced(1L), awaitItem())
        }
    }

    private fun sampleAddress() = Address(
        id = 1L,
        receiverName = "张三",
        phone = "13800138000",
        region = "广东省深圳市南山区",
        detail = "科技园",
        isDefault = true,
        updatedAt = Instant.now(),
    )
}

private class FakeCartRepository : CartRepository {
    override fun observeItems(): Flow<List<CartItem>> = flowOf(
        listOf(
            CartItem(
                productId = 1,
                title = "Item",
                unitPrice = 10.0,
                quantity = 1,
                imageUrl = "https://example.com/a.png",
                addedAt = Instant.now(),
            ),
        ),
    )

    override suspend fun addItem(product: com.zoti321.c2cmarket.domain.model.Product) =
        Result.success(Unit)

    override suspend fun updateQuantity(productId: Int, quantity: Int) = Result.success(Unit)

    override suspend fun removeItem(productId: Int) = Result.success(Unit)

    override suspend fun clearAll() = Result.success(Unit)
}

private class FakeAddressRepository(
    addresses: List<Address>,
) : AddressRepository {
    private val state = MutableStateFlow(addresses)

    override fun observeAll(): Flow<List<Address>> = state

    override fun observeDefault(): Flow<Address?> = flow {
        emit(state.value.firstOrNull { it.isDefault })
    }

    override suspend fun getById(id: Long) = Result.success(state.value.first { it.id == id })

    override suspend fun create(input: com.zoti321.c2cmarket.domain.model.AddressInput) =
        error("unused")

    override suspend fun update(id: Long, input: com.zoti321.c2cmarket.domain.model.AddressInput) =
        error("unused")

    override suspend fun delete(id: Long) = error("unused")

    override suspend fun setDefault(id: Long) = error("unused")
}

private class FakeOrderRepository : OrderRepository {
    override suspend fun placeOrder(shipping: ShippingInfo): Result<Order> =
        Result.success(
            Order(
                id = 1L,
                guestId = "guest",
                items = emptyList(),
                totalAmount = 10.0,
                status = OrderStatus.COMPLETED,
                createdAt = Instant.now(),
                shipping = shipping,
            ),
        )

    override fun observeOrders(): Flow<List<OrderSummary>> = flowOf(emptyList())

    override fun observeOrder(orderId: Long) = flowOf(null)
}
