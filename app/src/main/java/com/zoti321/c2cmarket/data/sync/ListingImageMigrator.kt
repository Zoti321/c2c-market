package com.zoti321.c2cmarket.data.sync

import android.net.Uri
import com.zoti321.c2cmarket.data.local.dao.ListingDao
import com.zoti321.c2cmarket.domain.datasource.ListingRemoteDataSource
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first

@Singleton
class ListingImageMigrator @Inject constructor(
    private val listingDao: ListingDao,
    private val listingRemote: ListingRemoteDataSource,
) {
    suspend fun migrateContentUrisForSeller(sellerId: String) {
        val listings = listingDao.observeBySellerId(sellerId).first()
        listings.filter { it.imageUri.startsWith("content://") }.forEach { listing ->
            listingRemote.uploadListingImage(listing.catalogId, Uri.parse(listing.imageUri))
                .onSuccess { httpsUrl ->
                    val updated = listing.copy(
                        imageUri = httpsUrl,
                        updatedAt = System.currentTimeMillis(),
                    )
                    listingDao.update(updated)
                    listingRemote.syncListing(updated)
                }
        }
        listingRemote.migratePendingImages(sellerId)
    }
}
