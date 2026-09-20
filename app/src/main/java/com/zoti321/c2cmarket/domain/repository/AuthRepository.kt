package com.zoti321.c2cmarket.domain.repository

import android.app.Activity
import com.zoti321.c2cmarket.domain.model.AuthState
import com.zoti321.c2cmarket.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun observeAuthState(): Flow<AuthState>

    fun currentUserId(): Flow<String>

    suspend fun signInWithGoogle(activity: Activity): Result<UserProfile>

    suspend fun signOut()
}
