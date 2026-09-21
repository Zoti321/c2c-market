package com.zoti321.c2cmarket.data.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.tasks.await

@Singleton
class UserMappingRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth,
) : UserMappingGateway {
    override suspend fun upsertBusinessUserId(businessUserId: String): Result<Unit> = runCatching {
        val uid = firebaseAuth.currentUser?.uid ?: error("No Firebase session")
        firestore.collection(COLLECTION)
            .document(uid)
            .set(
                mapOf(
                    FIELD_BUSINESS_USER_ID to businessUserId,
                    FIELD_UPDATED_AT to FieldValue.serverTimestamp(),
                ),
            )
            .await()
    }

    companion object {
        const val COLLECTION = "userMappings"
        const val FIELD_BUSINESS_USER_ID = "businessUserId"
        const val FIELD_UPDATED_AT = "updatedAt"
    }
}
