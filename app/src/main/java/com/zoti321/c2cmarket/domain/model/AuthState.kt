package com.zoti321.c2cmarket.domain.model

sealed interface AuthState {
    data object Guest : AuthState

    data class SignedIn(val profile: UserProfile) : AuthState
}

data class UserProfile(
    val userId: String,
    val displayName: String,
    val email: String?,
    val photoUrl: String?,
)

object UserIds {
    const val GUEST = "guest"

    fun google(sub: String): String = "google:$sub"
}
