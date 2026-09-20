package com.zoti321.c2cmarket.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.zoti321.c2cmarket.data.local.entity.FavoriteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {
    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE userId = :userId AND productId = :id)")
    fun observeIsFavorite(userId: String, id: Int): Flow<Boolean>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE userId = :userId AND productId = :id)")
    suspend fun isFavorite(userId: String, id: Int): Boolean

    @Query("SELECT * FROM favorites WHERE userId = :userId ORDER BY createdAt DESC")
    fun observeAll(userId: String): Flow<List<FavoriteEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entity: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE userId = :userId AND productId = :id")
    suspend fun deleteByProductId(userId: String, id: Int)

    @Query("DELETE FROM favorites WHERE productId = :id")
    suspend fun deleteByProductIdAllUsers(id: Int)

    @Query("UPDATE favorites SET userId = :newUserId WHERE userId = 'guest'")
    suspend fun migrateGuestFavorites(newUserId: String)
}
