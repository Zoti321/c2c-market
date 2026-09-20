package com.zoti321.c2cmarket.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.zoti321.c2cmarket.data.local.entity.BrowseHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BrowseHistoryDao {
    @Upsert
    suspend fun upsert(item: BrowseHistoryEntity)

    @Query("SELECT * FROM browse_history ORDER BY viewedAt DESC LIMIT 50")
    fun observeRecent(): Flow<List<BrowseHistoryEntity>>

    @Query(
        """
        DELETE FROM browse_history
        WHERE productId NOT IN (
            SELECT productId FROM browse_history ORDER BY viewedAt DESC LIMIT :max
        )
        """,
    )
    suspend fun trimToMax(max: Int = 50)

    @Query("DELETE FROM browse_history")
    suspend fun clearAll()
}
