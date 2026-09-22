package com.zoti321.c2cmarket.data.sync.merger

import com.zoti321.c2cmarket.data.firebase.ListingStatusTransitions
import com.zoti321.c2cmarket.data.local.dao.ListingDao
import com.zoti321.c2cmarket.data.local.entity.ListingEntity
import com.zoti321.c2cmarket.domain.datasource.RemoteListing
import com.zoti321.c2cmarket.domain.model.ListingStatus
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ListingRemoteMerger @Inject constructor(
    private val listingDao: ListingDao,
) {
    suspend fun mergeAll(remote: List<RemoteListing>) {
        remote.forEach { item ->
            val existing = listingDao.getByCatalogId(item.catalogId)
            if (existing == null) {
                listingDao.insert(item.toEntity())
            } else {
                mergeExisting(existing, item)
            }
        }
    }

    private suspend fun mergeExisting(existing: ListingEntity, item: RemoteListing) {
        val currentStatus = runCatching { ListingStatus.valueOf(existing.status) }
            .getOrDefault(ListingStatus.AVAILABLE)
        val mergedStatus = if (ListingStatusTransitions.canTransition(currentStatus, item.status)) {
            item.status.name
        } else {
            existing.status
        }
        val useRemoteFields = item.updatedAt >= existing.updatedAt
        listingDao.update(
            existing.copy(
                title = if (useRemoteFields) item.title else existing.title,
                price = if (useRemoteFields) item.price else existing.price,
                description = if (useRemoteFields) item.description else existing.description,
                category = if (useRemoteFields) item.category else existing.category,
                imageUri = if (useRemoteFields) item.imageUrl else existing.imageUri,
                meetupLocation = if (useRemoteFields) item.meetupLocation else existing.meetupLocation,
                status = mergedStatus,
                updatedAt = maxOf(existing.updatedAt, item.updatedAt),
            ),
        )
    }

    private fun RemoteListing.toEntity(): ListingEntity = ListingEntity(
        catalogId = catalogId,
        title = title,
        price = price,
        description = description,
        category = category,
        imageUri = imageUrl,
        sellerId = sellerId,
        status = status.name,
        meetupLocation = meetupLocation,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}
