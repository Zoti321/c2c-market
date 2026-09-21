package com.zoti321.c2cmarket.data.remote

import android.net.Uri
import com.zoti321.c2cmarket.data.local.entity.ListingEntity
import com.zoti321.c2cmarket.domain.datasource.ListingRemoteDataSource
import com.zoti321.c2cmarket.domain.datasource.RemoteListing
import com.zoti321.c2cmarket.domain.model.ListingStatus
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

@Singleton
class NoOpListingRemoteDataSource @Inject constructor() : ListingRemoteDataSource {
    override suspend fun uploadListingImage(catalogId: Int, localContentUri: Uri): Result<String> =
        Result.success(localContentUri.toString())

    override suspend fun deleteListingImage(catalogId: Int): Result<Unit> = Result.success(Unit)

    override suspend fun migratePendingImages(sellerId: String): Result<Int> = Result.success(0)

    override suspend fun syncListing(listing: ListingEntity): Result<Unit> = Result.success(Unit)

    override suspend fun syncListingStatus(catalogId: Int, status: ListingStatus): Result<Unit> =
        Result.success(Unit)

    override suspend fun deleteListing(catalogId: Int): Result<Unit> = Result.success(Unit)

    override fun observeRemoteListingsAsSeller(userId: String): Flow<List<RemoteListing>> =
        flowOf(emptyList())

    override fun observeAvailableListings(): Flow<List<RemoteListing>> = flowOf(emptyList())
}
