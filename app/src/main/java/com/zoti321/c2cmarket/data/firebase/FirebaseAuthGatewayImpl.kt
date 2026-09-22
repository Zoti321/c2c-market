package com.zoti321.c2cmarket.data.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.tasks.await

@Singleton
class FirebaseAuthGatewayImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
) : FirebaseAuthGateway {

    override suspend fun signInWithGoogleIdToken(idToken: String): Result<String> = runCatching {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val result = firebaseAuth.signInWithCredential(credential).await()
        result.user?.uid ?: error("Firebase sign-in returned no user")
    }

    override suspend fun signOut() {
        firebaseAuth.signOut()
    }

    override fun currentFirebaseUid(): String? = firebaseAuth.currentUser?.uid

    override fun isSignedIn(): Boolean = firebaseAuth.currentUser != null
}
