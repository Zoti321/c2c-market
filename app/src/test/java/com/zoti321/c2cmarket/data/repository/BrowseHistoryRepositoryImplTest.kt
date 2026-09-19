package com.zoti321.c2cmarket.data.repository

import com.zoti321.c2cmarket.data.local.dao.BrowseHistoryDao
import com.zoti321.c2cmarket.data.local.entity.BrowseHistoryEntity
import com.zoti321.c2cmarket.domain.model.Product
import com.zoti321.c2cmarket.domain.model.Rating
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class BrowseHistoryRepositoryImplTest {

    private lateinit var dao: FakeBrowseHistoryDao
    private lateinit var repository: BrowseHistoryRepositoryImpl

    private val product = Product(
        id = 1,
        title = "Phone",
        price = 99.0,
        description = "Desc",
        category = "electronics",
        imageUrl = "https://example.com/a.png",
        rating = Rating(4.0, 10),
    )

    @Before
    fun setUp() {
        dao = FakeBrowseHistoryDao()
        repository = BrowseHistoryRepositoryImpl(dao)
    }

    @Test
    fun recordView_upsertsSameProduct() = runTest {
        repository.recordView(product)
        repository.recordView(product.copy(title = "Phone 2"))

        val items = repository.observeRecent().first()
        assertEquals(1, items.size)
        assertEquals("Phone 2", items.first().title)
    }

    @Test
    fun clearAll_removesHistory() = runTest {
        repository.recordView(product)
        repository.clearAll()
        assertTrue(repository.observeRecent().first().isEmpty())
    }
}

private class FakeBrowseHistoryDao : BrowseHistoryDao {
    val items = MutableStateFlow<List<BrowseHistoryEntity>>(emptyList())

    override suspend fun upsert(item: BrowseHistoryEntity) {
        val updated = items.value.filterNot { it.productId == item.productId } + item
        items.value = updated.sortedByDescending { it.viewedAt }
    }

    override fun observeRecent(): Flow<List<BrowseHistoryEntity>> = items.asStateFlow()

    override suspend fun trimToMax(max: Int) {
        items.value = items.value.sortedByDescending { it.viewedAt }.take(max)
    }

    override suspend fun clearAll() {
        items.value = emptyList()
    }
}
