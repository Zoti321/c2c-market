package com.zoti321.c2cmarket.data.local

import com.zoti321.c2cmarket.data.local.entity.AddressEntity
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
class AddressDaoTest {

    private lateinit var database: C2CDatabase

    @Before
    fun setUp() {
        database = InMemoryDatabaseFactory.create()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun setDefaultAddress_keepsSingleDefault() = runTest {
        val dao = database.addressDao()
        val firstId = dao.insert(sampleAddress("张三", isDefault = true, updatedAt = 1L))
        val secondId = dao.insert(sampleAddress("李四", isDefault = false, updatedAt = 2L))

        dao.setDefaultAddress(secondId, updatedAt = 3L)

        val all = dao.observeAll().first()
        assertFalse(all.first { it.id == firstId }.isDefault)
        assertTrue(all.first { it.id == secondId }.isDefault)
        assertEquals(1, all.count { it.isDefault })
    }

    private fun sampleAddress(name: String, isDefault: Boolean, updatedAt: Long) = AddressEntity(
        receiverName = name,
        phone = "13800138000",
        region = "广东省深圳市南山区",
        detail = "科技园",
        isDefault = isDefault,
        updatedAt = updatedAt,
    )
}
