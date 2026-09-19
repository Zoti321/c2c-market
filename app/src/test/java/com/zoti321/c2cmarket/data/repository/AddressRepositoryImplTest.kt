package com.zoti321.c2cmarket.data.repository

import com.zoti321.c2cmarket.data.error.InvalidAddressException
import com.zoti321.c2cmarket.data.local.InMemoryDatabaseFactory
import com.zoti321.c2cmarket.domain.model.AddressInput
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class AddressRepositoryImplTest {

    private lateinit var database: com.zoti321.c2cmarket.data.local.C2CDatabase
    private lateinit var repository: AddressRepositoryImpl

    @Before
    fun setUp() {
        database = InMemoryDatabaseFactory.create()
        repository = AddressRepositoryImpl(database, database.addressDao())
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun create_secondAddressWithDefault_clearsPreviousDefault() = runTest {
        val first = repository.create(validInput(isDefault = true)).getOrThrow()
        val second = repository.create(validInput(isDefault = true).copy(receiverName = "李四"))
            .getOrThrow()

        val all = repository.observeAll().first()
        assertEquals(2, all.size)
        assertFalse(all.first { it.id == first.id }.isDefault)
        assertTrue(all.first { it.id == second.id }.isDefault)
    }

    @Test
    fun delete_defaultAddress_promotesNextDefault() = runTest {
        val first = repository.create(validInput(isDefault = true)).getOrThrow()
        repository.create(
            validInput(isDefault = false).copy(
                receiverName = "李四",
                phone = "13900139000",
            ),
        )
        repository.delete(first.id)

        val remaining = repository.observeAll().first()
        assertEquals(1, remaining.size)
        assertTrue(remaining.first().isDefault)
    }

    @Test
    fun create_invalidPhone_failsValidation() = runTest {
        val result = repository.create(validInput().copy(phone = "12345"))

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is InvalidAddressException)
    }

    private fun validInput(isDefault: Boolean = false) = AddressInput(
        receiverName = "张三",
        phone = "13800138000",
        region = "广东省深圳市南山区",
        detail = "科技园",
        isDefault = isDefault,
    )
}
