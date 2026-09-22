package com.zoti321.c2cmarket.data.firebase

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.zoti321.c2cmarket.data.local.entity.ListingEntity
import com.zoti321.c2cmarket.domain.datasource.ListingRemoteDataSource
import com.zoti321.c2cmarket.domain.datasource.RemoteListing
import com.zoti321.c2cmarket.domain.model.ListingStatus
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

@Singleton
@Suppress("TooManyFunctions")
class FirebaseListingRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val firebaseAuth: FirebaseAuth,
    private val imageCompressor: ImageCompressor,
) : ListingRemoteDataSource {

    override suspend fun uploadListingImage(catalogId: Int, localContentUri: Uri): Result<String> =
        runCatching {
            val uid = firebaseAuth.currentUser?.uid ?: error("No Firebase session")
            val compressed = imageCompressor.compressToJpeg(localContentUri).getOrThrow()
            try {
                val ref = storage.reference.child(storagePath(uid, catalogId))
                ref.putFile(android.net.Uri.fromFile(compressed)).await()
                ref.downloadUrl.await().toString()
            } finally {
                compressed.delete()
            }
        }

    override suspend fun deleteListingImage(catalogId: Int): Result<Unit> = runCatching {
        val uid = firebaseAuth.currentUser?.uid ?: return Result.success(Unit)
        storage.reference.child(storagePath(uid, catalogId)).delete().await()
    }

    override suspend fun migratePendingImages(sellerId: String): Result<Int> = Result.success(0)

    override suspend fun syncListing(listing: ListingEntity): Result<Unit> = runCatching {
        firestore.collection(COLLECTION_LISTINGS)
            .document(listing.catalogId.toString())
            .set(listing.toRemoteMap())
            .await()
    }

    override suspend fun syncListingStatus(catalogId: Int, status: ListingStatus): Result<Unit> =
        runCatching {
            firestore.collection(COLLECTION_LISTINGS)
                .document(catalogId.toString())
                .update(
                    mapOf(
                        FIELD_STATUS to status.name,
                        FIELD_UPDATED_AT to System.currentTimeMillis(),
                    ),
                )
                .await()
        }

    override suspend fun deleteListing(catalogId: Int): Result<Unit> = runCatching {
        firestore.collection(COLLECTION_LISTINGS)
            .document(catalogId.toString())
            .delete()
            .await()
    }

    override fun observeRemoteListingsAsSeller(userId: String): Flow<List<RemoteListing>> =
        observeListings(FIELD_SELLER_ID, userId)

    override fun observeAvailableListings(): Flow<List<RemoteListing>> =
        observeListings(FIELD_STATUS, ListingStatus.AVAILABLE.name)

    private fun observeListings(field: String, value: String): Flow<List<RemoteListing>> =
        callbackFlow {
            val registration = firestore.collection(COLLECTION_LISTINGS)
                .whereEqualTo(field, value)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) return@addSnapshotListener
                    val items = snapshot?.documents.orEmpty().mapNotNull { it.toRemoteListing() }
                    trySend(items)
                }
            awaitClose { registration.remove() }
        }

    private fun storagePath(firebaseUid: String, catalogId: Int): String =
        "users/$firebaseUid/listings/$catalogId/cover.jpg"

    private fun ListingEntity.toRemoteMap(): Map<String, Any?> = mapOf(
        FIELD_SELLER_ID to sellerId,
        FIELD_TITLE to title,
        FIELD_PRICE to price,
        FIELD_DESCRIPTION to description,
        FIELD_CATEGORY to category,
        FIELD_IMAGE_URL to imageUri,
        FIELD_MEETUP_LOCATION to meetupLocation,
        FIELD_STATUS to status,
        FIELD_CREATED_AT to createdAt,
        FIELD_UPDATED_AT to updatedAt,
    )

    private fun com.google.firebase.firestore.DocumentSnapshot.toRemoteListing(): RemoteListing? {
        val catalogId = id.toIntOrNull()
        val sellerId = getString(FIELD_SELLER_ID)
        val statusName = getString(FIELD_STATUS) ?: ListingStatus.AVAILABLE.name
        val status = runCatching { ListingStatus.valueOf(statusName) }
            .getOrDefault(ListingStatus.AVAILABLE)
        if (catalogId == null || sellerId == null) {
            return null
        }
        return RemoteListing(
            catalogId = catalogId,
            sellerId = sellerId,
            title = getString(FIELD_TITLE).orEmpty(),
            price = getDouble(FIELD_PRICE) ?: 0.0,
            description = getString(FIELD_DESCRIPTION).orEmpty(),
            category = getString(FIELD_CATEGORY).orEmpty(),
            imageUrl = getString(FIELD_IMAGE_URL).orEmpty(),
            meetupLocation = getString(FIELD_MEETUP_LOCATION),
            status = status,
            createdAt = getLong(FIELD_CREATED_AT) ?: 0L,
            updatedAt = getLong(FIELD_UPDATED_AT) ?: 0L,
        )
    }

    companion object {
        const val COLLECTION_LISTINGS = "listings"
        const val FIELD_SELLER_ID = "sellerId"
        const val FIELD_TITLE = "title"
        const val FIELD_PRICE = "price"
        const val FIELD_DESCRIPTION = "description"
        const val FIELD_CATEGORY = "category"
        const val FIELD_IMAGE_URL = "imageUrl"
        const val FIELD_MEETUP_LOCATION = "meetupLocation"
        const val FIELD_STATUS = "status"
        const val FIELD_CREATED_AT = "createdAt"
        const val FIELD_UPDATED_AT = "updatedAt"
    }
}
