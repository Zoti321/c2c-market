package com.zoti321.c2cmarket.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.zoti321.c2cmarket.data.local.entity.ListingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ListingDao {
    @Query("SELECT * FROM listings ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<ListingEntity>>

    @Query("SELECT * FROM listings WHERE status = 'AVAILABLE' ORDER BY createdAt DESC")
    fun observeAvailable(): Flow<List<ListingEntity>>

    @Query("SELECT * FROM listings WHERE sellerId = :sellerId ORDER BY createdAt DESC")
    fun observeBySellerId(sellerId: String): Flow<List<ListingEntity>>

    @Query("UPDATE listings SET sellerId = :newUserId WHERE sellerId = 'guest'")
    suspend fun migrateGuestListings(newUserId: String)

    @Query("SELECT * FROM listings WHERE category = :category AND status = 'AVAILABLE' ORDER BY createdAt DESC")
    fun observeAvailableByCategory(category: String): Flow<List<ListingEntity>>

    @Query("SELECT * FROM listings WHERE catalogId = :catalogId LIMIT 1")
    suspend fun getByCatalogId(catalogId: Int): ListingEntity?

    @Query("SELECT MIN(catalogId) FROM listings")
    suspend fun minCatalogId(): Int?

    @Insert
    suspend fun insert(entity: ListingEntity): Long

    @Update
    suspend fun update(entity: ListingEntity)

    @Query("UPDATE listings SET status = :status, updatedAt = :updatedAt WHERE catalogId = :catalogId")
    suspend fun updateStatus(catalogId: Int, status: String, updatedAt: Long)

    @Query(
        """
        UPDATE listings SET status = 'RESERVED', updatedAt = :updatedAt
        WHERE catalogId IN (:catalogIds) AND status = 'AVAILABLE'
        """,
    )
    suspend fun markReserved(catalogIds: List<Int>, updatedAt: Long)

    @Query("DELETE FROM listings WHERE catalogId = :catalogId")
    suspend fun deleteByCatalogId(catalogId: Int)
}
