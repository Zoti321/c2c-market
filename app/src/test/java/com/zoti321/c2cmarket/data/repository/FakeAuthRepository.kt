package com.zoti321.c2cmarket.data.repository

import android.app.Activity
import com.zoti321.c2cmarket.domain.model.AuthState
import com.zoti321.c2cmarket.domain.model.UserIds
import com.zoti321.c2cmarket.domain.model.UserProfile
import com.zoti321.c2cmarket.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

class FakeAuthRepository(
    initialUserId: String = UserIds.GUEST,
    private val profile: UserProfile? = null,
) : AuthRepository {

    private val authState = MutableStateFlow<AuthState>(
        if (initialUserId == UserIds.GUEST) {
            AuthState.Guest
        } else {
            AuthState.SignedIn(
                profile ?: UserProfile(
                    userId = initialUserId,
                    displayName = "Test User",
                    email = "test@example.com",
                    photoUrl = null,
                ),
            )
        },
    )

    override fun observeAuthState(): Flow<AuthState> = authState.asStateFlow()

    override fun currentUserId(): Flow<String> = authState.map { state ->
        when (state) {
            AuthState.Guest -> UserIds.GUEST
            is AuthState.SignedIn -> state.profile.userId
        }
    }

    override suspend fun signInWithGoogle(activity: Activity): Result<UserProfile> =
        Result.failure(UnsupportedOperationException())

    override suspend fun signOut() {
        authState.value = AuthState.Guest
    }

    fun setSignedIn(userProfile: UserProfile) {
        authState.value = AuthState.SignedIn(userProfile)
    }
}
