package com.zoti321.c2cmarket.data.local

import com.zoti321.c2cmarket.data.local.entity.ListingEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class ListingDaoTest {

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
    fun insert_rejectsDuplicateCatalogId() = runTest {
        val dao = database.listingDao()
        dao.insert(sampleListing(catalogId = -1))

        val duplicateResult = runCatching {
            dao.insert(sampleListing(catalogId = -1, title = "Duplicate"))
        }

        assertTrue(duplicateResult.isFailure)
        assertEquals(1, dao.observeAll().first().size)
    }

    private fun sampleListing(catalogId: Int, title: String = "Phone") = ListingEntity(
        catalogId = catalogId,
        title = title,
        price = 9.99,
        description = "Desc",
        category = "electronics",
        imageUri = "content://test/image",
        createdAt = 1L,
        updatedAt = 1L,
    )
}
