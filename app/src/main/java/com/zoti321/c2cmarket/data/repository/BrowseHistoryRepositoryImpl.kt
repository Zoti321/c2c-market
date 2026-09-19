package com.zoti321.c2cmarket.data.repository

import com.zoti321.c2cmarket.data.local.dao.BrowseHistoryDao
import com.zoti321.c2cmarket.data.mapper.toBrowseHistoryEntity
import com.zoti321.c2cmarket.data.mapper.toDomain
import com.zoti321.c2cmarket.domain.model.BrowseHistoryItem
import com.zoti321.c2cmarket.domain.model.Product
import com.zoti321.c2cmarket.domain.repository.BrowseHistoryRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class BrowseHistoryRepositoryImpl @Inject constructor(
    private val browseHistoryDao: BrowseHistoryDao,
) : BrowseHistoryRepository {

    override fun observeRecent(): Flow<List<BrowseHistoryItem>> =
        browseHistoryDao.observeRecent().map { items -> items.map { it.toDomain() } }

    override suspend fun recordView(product: Product): Result<Unit> = runCatching {
        val now = System.currentTimeMillis()
        browseHistoryDao.upsert(product.toBrowseHistoryEntity(now))
        browseHistoryDao.trimToMax(max = 50)
    }

    override suspend fun clearAll(): Result<Unit> = runCatching {
        browseHistoryDao.clearAll()
    }
}
