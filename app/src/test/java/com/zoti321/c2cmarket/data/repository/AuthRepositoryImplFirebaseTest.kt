package com.zoti321.c2cmarket.data.repository

import android.app.Activity
import com.zoti321.c2cmarket.data.auth.GoogleCredentialDataSource
import com.zoti321.c2cmarket.data.auth.GoogleSignInResult
import com.zoti321.c2cmarket.data.auth.TestDataStoreFactory
import com.zoti321.c2cmarket.data.firebase.UserMappingGateway
import com.zoti321.c2cmarket.data.migration.GuestDataMigration
import com.zoti321.c2cmarket.domain.model.UserIds
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class AuthRepositoryImplFirebaseTest {

    private lateinit var firebaseAuth: FakeFirebaseAuthGateway
    private lateinit var userMapping: RecordingUserMappingGateway
    private lateinit var repository: AuthRepositoryImpl

    @Before
    fun setUp() {
        firebaseAuth = FakeFirebaseAuthGateway()
        userMapping = RecordingUserMappingGateway()
        repository = AuthRepositoryImpl(
            dataStore = TestDataStoreFactory.create(),
            googleCredentialDataSource = FixedGoogleCredentialDataSource(),
            guestDataMigration = NoOpGuestDataMigration(),
            firebaseAuthGateway = firebaseAuth,
            userMappingGateway = userMapping,
            remoteSyncGateway = lazyRemoteSync(),
        )
    }

    @Test
    fun signInWithGoogle_establishesFirebaseSessionAndMapping() = runTest {
        repository.signInWithGoogle(FakeActivity()).getOrThrow()

        assertTrue(firebaseAuth.isSignedIn())
        assertEquals(UserIds.google("firebase-sub"), userMapping.lastBusinessUserId)
    }

    @Test
    fun signOut_clearsFirebaseSession() = runTest {
        repository.signInWithGoogle(FakeActivity()).getOrThrow()

        repository.signOut()

        assertTrue(!firebaseAuth.isSignedIn())
    }

    private class FixedGoogleCredentialDataSource : GoogleCredentialDataSource {
        override suspend fun signIn(activity: Activity, webClientId: String): Result<GoogleSignInResult> =
            Result.success(
                GoogleSignInResult(
                    sub = "firebase-sub",
                    displayName = "Test",
                    email = "test@example.com",
                    photoUrl = null,
                    idToken = "id-token",
                ),
            )
    }

    private class NoOpGuestDataMigration : GuestDataMigration {
        override suspend fun migrateGuestDataTo(newUserId: String) = Unit
    }

    private class RecordingUserMappingGateway : UserMappingGateway {
        var lastBusinessUserId: String? = null
            private set

        override suspend fun upsertBusinessUserId(businessUserId: String): Result<Unit> {
            lastBusinessUserId = businessUserId
            return Result.success(Unit)
        }
    }

    private class FakeActivity : Activity()
}
