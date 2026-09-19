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

    @Query("SELECT * FROM listings WHERE category = :category ORDER BY createdAt DESC")
    fun observeByCategory(category: String): Flow<List<ListingEntity>>

    @Query("SELECT * FROM listings WHERE catalogId = :catalogId LIMIT 1")
    suspend fun getByCatalogId(catalogId: Int): ListingEntity?

    @Query("SELECT MIN(catalogId) FROM listings")
    suspend fun minCatalogId(): Int?

    @Insert
    suspend fun insert(entity: ListingEntity): Long

    @Update
    suspend fun update(entity: ListingEntity)

    @Query("DELETE FROM listings WHERE catalogId = :catalogId")
    suspend fun deleteByCatalogId(catalogId: Int)
}
