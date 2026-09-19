package com.zoti321.c2cmarket.data.local

import com.zoti321.c2cmarket.data.local.entity.BrowseHistoryEntity
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
class BrowseHistoryDaoTest {

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
    fun upsert_updatesViewedAtForSameProduct() = runTest {
        val dao = database.browseHistoryDao()
        dao.upsert(
            BrowseHistoryEntity(
                productId = 1,
                title = "Phone",
                imageUrl = "https://example.com/a.png",
                price = 9.99,
                viewedAt = 100L,
            ),
        )
        dao.upsert(
            BrowseHistoryEntity(
                productId = 1,
                title = "Phone",
                imageUrl = "https://example.com/a.png",
                price = 9.99,
                viewedAt = 200L,
            ),
        )

        val items = dao.observeRecent().first()
        assertEquals(1, items.size)
        assertEquals(200L, items.first().viewedAt)
        assertTrue(items.first().viewedAt > 100L)
    }
}
