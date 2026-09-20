package com.zoti321.c2cmarket.data.repository

import android.app.Activity
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.zoti321.c2cmarket.BuildConfig
import com.zoti321.c2cmarket.data.auth.GoogleCredentialDataSource
import com.zoti321.c2cmarket.data.auth.clearSession
import com.zoti321.c2cmarket.data.auth.observeAuthState
import com.zoti321.c2cmarket.data.auth.saveProfile
import com.zoti321.c2cmarket.data.migration.GuestDataMigration
import com.zoti321.c2cmarket.domain.model.AuthState
import com.zoti321.c2cmarket.domain.model.UserIds
import com.zoti321.c2cmarket.domain.model.UserProfile
import com.zoti321.c2cmarket.domain.repository.AuthRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val googleCredentialDataSource: GoogleCredentialDataSource,
    private val guestDataMigration: GuestDataMigration,
) : AuthRepository {

    override fun observeAuthState(): Flow<AuthState> = dataStore.observeAuthState()

    override fun currentUserId(): Flow<String> = observeAuthState().map { state ->
        when (state) {
            AuthState.Guest -> UserIds.GUEST
            is AuthState.SignedIn -> state.profile.userId
        }
    }

    override suspend fun signInWithGoogle(activity: Activity): Result<UserProfile> = runCatching {
        val signInResult = googleCredentialDataSource
            .signIn(activity, BuildConfig.GOOGLE_WEB_CLIENT_ID)
            .getOrThrow()
        val profile = UserProfile(
            userId = UserIds.google(signInResult.sub),
            displayName = signInResult.displayName.ifBlank { "Google 用户" },
            email = signInResult.email,
            photoUrl = signInResult.photoUrl,
        )
        dataStore.saveProfile(profile)
        guestDataMigration.migrateGuestDataTo(profile.userId)
        profile
    }

    override suspend fun signOut() {
        dataStore.clearSession()
    }
}
