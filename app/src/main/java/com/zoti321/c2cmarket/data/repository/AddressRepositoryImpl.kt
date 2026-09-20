package com.zoti321.c2cmarket.data.repository

import androidx.room.withTransaction
import com.zoti321.c2cmarket.data.error.InvalidAddressException
import com.zoti321.c2cmarket.data.local.C2CDatabase
import com.zoti321.c2cmarket.data.local.dao.AddressDao
import com.zoti321.c2cmarket.data.mapper.toDomain
import com.zoti321.c2cmarket.data.mapper.toEntity
import com.zoti321.c2cmarket.domain.model.Address
import com.zoti321.c2cmarket.domain.model.AddressInput
import com.zoti321.c2cmarket.domain.repository.AddressRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

@Singleton
class AddressRepositoryImpl @Inject constructor(
    private val database: C2CDatabase,
    private val addressDao: AddressDao,
) : AddressRepository {

    override fun observeAll(): Flow<List<Address>> =
        addressDao.observeAll().map { list -> list.map { it.toDomain() } }

    override fun observeDefault(): Flow<Address?> =
        addressDao.observeDefault().map { entity -> entity?.toDomain() }

    override suspend fun getById(id: Long): Result<Address> = runCatching {
        addressDao.getById(id)?.toDomain()
            ?: throw InvalidAddressException("地址不存在")
    }

    override suspend fun create(input: AddressInput): Result<Address> = runCatching {
        validate(input)
        val now = System.currentTimeMillis()
        val existingCount = addressDao.observeAll().first().size
        val shouldDefault = existingCount == 0 || input.isDefault
        val entity = input.copy(isDefault = shouldDefault).toEntity(updatedAt = now)
        database.withTransaction {
            if (shouldDefault) {
                addressDao.clearAllDefaults()
            }
            val id = addressDao.insert(entity)
            entity.copy(id = id).toDomain()
        }
    }

    override suspend fun update(id: Long, input: AddressInput): Result<Address> = runCatching {
        validate(input)
        val existing = addressDao.getById(id)
            ?: throw InvalidAddressException("地址不存在")
        val now = System.currentTimeMillis()
        val updated = input.toEntity(id = id, updatedAt = now)
        database.withTransaction {
            if (input.isDefault) {
                addressDao.clearAllDefaults()
            }
            addressDao.update(updated.copy(isDefault = input.isDefault || existing.isDefault))
            updated.copy(isDefault = input.isDefault || existing.isDefault).toDomain()
        }
    }

    override suspend fun delete(id: Long): Result<Unit> = runCatching {
        val existing = addressDao.getById(id)
            ?: throw InvalidAddressException("地址不存在")
        database.withTransaction {
            addressDao.deleteById(id)
            if (existing.isDefault) {
                val remaining = addressDao.observeAll().first()
                if (remaining.isNotEmpty()) {
                    val next = remaining.maxByOrNull { it.updatedAt }!!
                    addressDao.setDefaultAddress(next.id, System.currentTimeMillis())
                }
            }
        }
    }

    override suspend fun setDefault(id: Long): Result<Unit> = runCatching {
        val existing = addressDao.getById(id)
            ?: throw InvalidAddressException("地址不存在")
        addressDao.setDefaultAddress(existing.id, System.currentTimeMillis())
    }

    private fun validate(input: AddressInput) {
        val message = when {
            input.receiverName.isBlank() || input.receiverName.length > 20 -> "收货人无效"
            !PHONE_REGEX.matches(input.phone) -> "手机号格式无效"
            input.region.isBlank() || input.region.length > 50 -> "省市区无效"
            input.detail.isBlank() || input.detail.length > 100 -> "详细地址无效"
            else -> null
        }
        if (message != null) throw InvalidAddressException(message)
    }

    companion object {
        private val PHONE_REGEX = Regex("^1[3-9]\\d{9}$")
    }
}
