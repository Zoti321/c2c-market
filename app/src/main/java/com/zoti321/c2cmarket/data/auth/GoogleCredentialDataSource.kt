package com.zoti321.c2cmarket.data.auth

import android.app.Activity

data class GoogleSignInResult(
    val sub: String,
    val displayName: String,
    val email: String?,
    val photoUrl: String?,
    val idToken: String,
)

class MissingWebClientIdException :
    IllegalStateException("Google Web Client ID 未配置，请在 local.properties 中设置 google.web_client_id")

interface GoogleCredentialDataSource {
    suspend fun signIn(activity: Activity, webClientId: String): Result<GoogleSignInResult>
}
