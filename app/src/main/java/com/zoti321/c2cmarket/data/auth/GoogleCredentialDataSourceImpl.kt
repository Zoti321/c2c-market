package com.zoti321.c2cmarket.data.auth

import android.app.Activity
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import javax.inject.Inject
import javax.inject.Singleton
import org.json.JSONObject
import android.util.Base64

@Singleton
class GoogleCredentialDataSourceImpl @Inject constructor() : GoogleCredentialDataSource {

    override suspend fun signIn(activity: Activity, webClientId: String): Result<GoogleSignInResult> =
        runCatching {
            validateWebClientId(webClientId)
            val credentialManager = CredentialManager.create(activity)
            val response = try {
                credentialManager.getCredential(
                    activity,
                    buildGoogleIdRequest(webClientId),
                )
            } catch (_: NoCredentialException) {
                credentialManager.getCredential(
                    activity,
                    buildSignInWithGoogleRequest(webClientId),
                )
            }
            val googleCredential = GoogleIdTokenCredential.createFrom(response.credential.data)
            val idToken = googleCredential.idToken
                ?: error("Google ID Token missing")
            GoogleSignInResult(
                sub = googleCredential.id,
                displayName = googleCredential.displayName.orEmpty(),
                email = extractEmail(idToken),
                photoUrl = googleCredential.profilePictureUri?.toString(),
                idToken = idToken,
            )
        }

    private fun validateWebClientId(webClientId: String) {
        if (webClientId.isBlank() || webClientId.contains("YOUR_WEB_CLIENT_ID")) {
            throw MissingWebClientIdException()
        }
    }

    private fun buildGoogleIdRequest(webClientId: String): GetCredentialRequest {
        val option = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(webClientId)
            .build()
        return GetCredentialRequest.Builder()
            .addCredentialOption(option)
            .build()
    }

    private fun buildSignInWithGoogleRequest(webClientId: String): GetCredentialRequest {
        val option = GetSignInWithGoogleOption.Builder(webClientId).build()
        return GetCredentialRequest.Builder()
            .addCredentialOption(option)
            .build()
    }

    private fun extractEmail(idToken: String?): String? =
        idToken?.takeIf { it.isNotBlank() }
            ?.split(".")
            ?.takeIf { it.size >= 2 }
            ?.let { parts ->
                runCatching {
                    val payload = String(Base64.decode(parts[1], Base64.URL_SAFE or Base64.NO_WRAP))
                    JSONObject(payload).optString("email").takeIf { it.isNotBlank() }
                }.getOrNull()
            }
}
