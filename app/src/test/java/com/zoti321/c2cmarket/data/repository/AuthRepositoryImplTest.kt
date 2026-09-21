package com.zoti321.c2cmarket.data.repository

import android.app.Activity
import app.cash.turbine.test
import com.zoti321.c2cmarket.data.auth.GoogleCredentialDataSource
import com.zoti321.c2cmarket.data.auth.GoogleSignInResult
import com.zoti321.c2cmarket.data.auth.TestDataStoreFactory
import com.zoti321.c2cmarket.data.firebase.UserMappingGateway
import com.zoti321.c2cmarket.data.migration.GuestDataMigration
import com.zoti321.c2cmarket.domain.model.AuthState
import com.zoti321.c2cmarket.domain.model.UserIds
import kotlinx.coroutines.flow.first
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
class AuthRepositoryImplTest {

    private lateinit var repository: AuthRepositoryImpl
    private lateinit var googleDataSource: RecordingGoogleCredentialDataSource
    private lateinit var guestDataMigration: RecordingGuestDataMigration
    private lateinit var firebaseAuthGateway: FakeFirebaseAuthGateway
    private lateinit var userMappingGateway: RecordingUserMappingGateway
    private lateinit var remoteSyncGateway: FakeRemoteSyncGateway

    @Before
    fun setUp() {
        googleDataSource = RecordingGoogleCredentialDataSource()
        guestDataMigration = RecordingGuestDataMigration()
        firebaseAuthGateway = FakeFirebaseAuthGateway()
        userMappingGateway = RecordingUserMappingGateway()
        remoteSyncGateway = FakeRemoteSyncGateway()
        repository = AuthRepositoryImpl(
            dataStore = TestDataStoreFactory.create(),
            googleCredentialDataSource = googleDataSource,
            guestDataMigration = guestDataMigration,
            firebaseAuthGateway = firebaseAuthGateway,
            userMappingGateway = userMappingGateway,
            remoteSyncGateway = lazyRemoteSync(remoteSyncGateway),
        )
    }

    @Test
    fun observeAuthState_startsAsGuest() = runTest {
        repository.observeAuthState().test {
            assertEquals(AuthState.Guest, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun signInWithGoogle_persistsProfileAndMigratesGuestData() = runTest {
        googleDataSource.result = Result.success(
            GoogleSignInResult(
                sub = "abc123",
                displayName = "Alice",
                email = "alice@example.com",
                photoUrl = "https://example.com/photo.jpg",
                idToken = "token-abc",
            ),
        )

        val profile = repository.signInWithGoogle(FakeActivity()).getOrThrow()

        assertEquals(UserIds.google("abc123"), profile.userId)
        assertEquals("Alice", profile.displayName)
        assertEquals(UserIds.google("abc123"), guestDataMigration.migratedTo)
        assertEquals(UserIds.google("abc123"), userMappingGateway.lastBusinessUserId)
        assertTrue(firebaseAuthGateway.isSignedIn())

        val state = repository.observeAuthState().first()
        assertTrue(state is AuthState.SignedIn)
        assertEquals(profile, (state as AuthState.SignedIn).profile)
    }

    @Test
    fun signOut_returnsToGuest() = runTest {
        googleDataSource.result = Result.success(
            GoogleSignInResult(
                sub = "abc123",
                displayName = "Alice",
                email = null,
                photoUrl = null,
                idToken = "token-abc",
            ),
        )
        repository.signInWithGoogle(FakeActivity()).getOrThrow()

        repository.signOut()

        assertTrue(!firebaseAuthGateway.isSignedIn())
        repository.observeAuthState().test {
            assertEquals(AuthState.Guest, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun signInWithGoogle_propagatesCredentialFailure() = runTest {
        googleDataSource.result = Result.failure(IllegalStateException("credential failed"))

        val result = repository.signInWithGoogle(FakeActivity())

        assertTrue(result.isFailure)
        assertEquals(0, guestDataMigration.migrationCount)
    }

    private class RecordingGoogleCredentialDataSource : GoogleCredentialDataSource {
        var result: Result<GoogleSignInResult> = Result.failure(IllegalStateException("unset"))

        override suspend fun signIn(activity: Activity, webClientId: String): Result<GoogleSignInResult> =
            result
    }

    private class RecordingGuestDataMigration : GuestDataMigration {
        var migratedTo: String? = null
            private set
        var migrationCount = 0
            private set

        override suspend fun migrateGuestDataTo(newUserId: String) {
            migratedTo = newUserId
            migrationCount++
        }
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
