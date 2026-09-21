package com.zoti321.c2cmarket.data.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.tasks.await

@Singleton
class FcmTokenRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth,
) {
    suspend fun upsertToken(token: String): Result<Unit> = runCatching {
        val uid = firebaseAuth.currentUser?.uid ?: error("No Firebase session")
        val tokenHash = token.sha256()
        firestore.collection(UserMappingRepository.COLLECTION)
            .document(uid)
            .collection(SUBCOLLECTION)
            .document(tokenHash)
            .set(
                mapOf(
                    FIELD_TOKEN to token,
                    UserMappingRepository.FIELD_UPDATED_AT to FieldValue.serverTimestamp(),
                ),
            )
            .await()
    }

    suspend fun deleteToken(token: String): Result<Unit> = runCatching {
        val uid = firebaseAuth.currentUser?.uid ?: return Result.success(Unit)
        val tokenHash = token.sha256()
        firestore.collection(UserMappingRepository.COLLECTION)
            .document(uid)
            .collection(SUBCOLLECTION)
            .document(tokenHash)
            .delete()
            .await()
    }

    private fun String.sha256(): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(toByteArray())
        return hash.joinToString("") { "%02x".format(it) }
    }

    companion object {
        const val SUBCOLLECTION = "fcmTokens"
        const val FIELD_TOKEN = "token"
    }
}
