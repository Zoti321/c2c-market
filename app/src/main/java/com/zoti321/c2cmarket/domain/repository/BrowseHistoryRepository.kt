package com.zoti321.c2cmarket.domain.repository

import com.zoti321.c2cmarket.domain.model.BrowseHistoryItem
import com.zoti321.c2cmarket.domain.model.Product
import kotlinx.coroutines.flow.Flow

interface BrowseHistoryRepository {
    fun observeRecent(): Flow<List<BrowseHistoryItem>>

    suspend fun recordView(product: Product): Result<Unit>

    suspend fun clearAll(): Result<Unit>
}
