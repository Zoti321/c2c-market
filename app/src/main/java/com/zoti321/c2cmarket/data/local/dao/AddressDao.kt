package com.zoti321.c2cmarket.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.zoti321.c2cmarket.data.local.entity.AddressEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AddressDao {
    @Query("SELECT * FROM addresses ORDER BY isDefault DESC, updatedAt DESC")
    fun observeAll(): Flow<List<AddressEntity>>

    @Query("SELECT * FROM addresses WHERE isDefault = 1 LIMIT 1")
    fun observeDefault(): Flow<AddressEntity?>

    @Query("SELECT * FROM addresses WHERE id = :id")
    suspend fun getById(id: Long): AddressEntity?

    @Insert
    suspend fun insert(entity: AddressEntity): Long

    @Update
    suspend fun update(entity: AddressEntity)

    @Query("DELETE FROM addresses WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE addresses SET isDefault = 0")
    suspend fun clearAllDefaults()

    @Query("UPDATE addresses SET isDefault = 1, updatedAt = :updatedAt WHERE id = :id")
    suspend fun markDefault(id: Long, updatedAt: Long)

    @Transaction
    suspend fun setDefaultAddress(id: Long, updatedAt: Long) {
        clearAllDefaults()
        markDefault(id, updatedAt)
    }
}
