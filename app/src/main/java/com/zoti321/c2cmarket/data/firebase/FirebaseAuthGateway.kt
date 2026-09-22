package com.zoti321.c2cmarket.data.firebase

interface FirebaseAuthGateway {
    suspend fun signInWithGoogleIdToken(idToken: String): Result<String>

    suspend fun signOut()

    fun currentFirebaseUid(): String?

    fun isSignedIn(): Boolean
}
