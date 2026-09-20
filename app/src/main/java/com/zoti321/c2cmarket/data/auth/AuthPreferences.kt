package com.zoti321.c2cmarket.data.auth

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.zoti321.c2cmarket.domain.model.AuthState
import com.zoti321.c2cmarket.domain.model.UserProfile
import com.zoti321.c2cmarket.domain.model.UserIds
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

object AuthPreferences {
    val USER_ID = stringPreferencesKey("auth_user_id")
    val DISPLAY_NAME = stringPreferencesKey("auth_display_name")
    val EMAIL = stringPreferencesKey("auth_email")
    val PHOTO_URL = stringPreferencesKey("auth_photo_url")
}

fun Preferences.toAuthState(): AuthState {
    val userId = this[AuthPreferences.USER_ID]
    if (userId.isNullOrBlank() || userId == UserIds.GUEST) {
        return AuthState.Guest
    }
    return AuthState.SignedIn(
        UserProfile(
            userId = userId,
            displayName = this[AuthPreferences.DISPLAY_NAME].orEmpty(),
            email = this[AuthPreferences.EMAIL],
            photoUrl = this[AuthPreferences.PHOTO_URL],
        ),
    )
}

fun DataStore<Preferences>.observeAuthState(): Flow<AuthState> =
    data.map { preferences -> preferences.toAuthState() }

suspend fun DataStore<Preferences>.saveProfile(profile: UserProfile) {
    edit { preferences ->
        preferences[AuthPreferences.USER_ID] = profile.userId
        preferences[AuthPreferences.DISPLAY_NAME] = profile.displayName
        profile.email?.let { preferences[AuthPreferences.EMAIL] = it }
            ?: preferences.remove(AuthPreferences.EMAIL)
        profile.photoUrl?.let { preferences[AuthPreferences.PHOTO_URL] = it }
            ?: preferences.remove(AuthPreferences.PHOTO_URL)
    }
}

suspend fun DataStore<Preferences>.clearSession() {
    edit { preferences ->
        preferences.remove(AuthPreferences.USER_ID)
        preferences.remove(AuthPreferences.DISPLAY_NAME)
        preferences.remove(AuthPreferences.EMAIL)
        preferences.remove(AuthPreferences.PHOTO_URL)
    }
}
