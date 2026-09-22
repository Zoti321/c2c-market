package com.zoti321.c2cmarket.domain.datasource

import android.net.Uri
import com.zoti321.c2cmarket.data.local.entity.ListingEntity
import com.zoti321.c2cmarket.domain.model.ListingStatus
import kotlinx.coroutines.flow.Flow

interface ListingRemoteDataSource {
    suspend fun uploadListingImage(catalogId: Int, localContentUri: Uri): Result<String>

    suspend fun deleteListingImage(catalogId: Int): Result<Unit>

    suspend fun migratePendingImages(sellerId: String): Result<Int>

    suspend fun syncListing(listing: ListingEntity): Result<Unit>

    suspend fun syncListingStatus(catalogId: Int, status: ListingStatus): Result<Unit>

    suspend fun deleteListing(catalogId: Int): Result<Unit>

    fun observeRemoteListingsAsSeller(userId: String): Flow<List<RemoteListing>>

    fun observeAvailableListings(): Flow<List<RemoteListing>>
}
