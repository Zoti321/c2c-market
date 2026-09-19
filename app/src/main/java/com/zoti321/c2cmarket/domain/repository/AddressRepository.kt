package com.zoti321.c2cmarket.domain.repository

import com.zoti321.c2cmarket.domain.model.Address
import com.zoti321.c2cmarket.domain.model.AddressInput
import kotlinx.coroutines.flow.Flow

interface AddressRepository {
    fun observeAll(): Flow<List<Address>>

    fun observeDefault(): Flow<Address?>

    suspend fun getById(id: Long): Result<Address>

    suspend fun create(input: AddressInput): Result<Address>

    suspend fun update(id: Long, input: AddressInput): Result<Address>

    suspend fun delete(id: Long): Result<Unit>

    suspend fun setDefault(id: Long): Result<Unit>
}
