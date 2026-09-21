package com.zoti321.c2cmarket.data.repository

import com.zoti321.c2cmarket.data.local.InMemoryDatabaseFactory
import com.zoti321.c2cmarket.domain.model.ListingInput
import com.zoti321.c2cmarket.domain.model.UserIds
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class ListingRepositoryImplSyncTest {

    private lateinit var database: com.zoti321.c2cmarket.data.local.C2CDatabase
    private lateinit var listingRemote: FakeListingRemoteDataSource
    private lateinit var deps: RepositoryTestDeps
    private lateinit var repository: ListingRepositoryImpl

    @Before
    fun setUp() {
        database = InMemoryDatabaseFactory.create()
        listingRemote = FakeListingRemoteDataSource()
        deps = createRepositoryTestDeps().copy(
            listingRemote = listingRemote,
            firebaseAuth = FakeFirebaseAuthGateway(signedIn = true),
        )
        repository = createListingRepository(
            database = database,
            authRepository = FakeAuthRepository(initialUserId = UserIds.google("seller-1")),
            deps = deps,
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun create_withContentUri_uploadsAndStoresHttps() = runTest(deps.dispatcher) {
        val product = repository.create(
            ListingInput(
                title = "Upload Test",
                price = 12.0,
                description = "desc",
                category = "electronics",
                imageUri = "content://test/image",
            ),
        ).getOrThrow()

        assertTrue(product.imageUrl.startsWith("https://"))
        assertEquals(1, listingRemote.syncedListings.size)
    }

    @Test
    fun create_withHttps_skipsReUpload() = runTest(deps.dispatcher) {
        val https = "https://example.com/existing.jpg"
        val product = repository.create(
            ListingInput(
                title = "HTTPS Test",
                price = 12.0,
                description = "desc",
                category = "electronics",
                imageUri = https,
            ),
        ).getOrThrow()

        assertEquals(https, product.imageUrl)
        assertEquals(1, listingRemote.syncedListings.size)
    }

    @Test
    fun create_guestWithContentUri_fails() = runTest(deps.dispatcher) {
        val guestRepo = createListingRepository(database, FakeAuthRepository(), deps)

        val result = guestRepo.create(
            ListingInput(
                title = "Guest",
                price = 12.0,
                description = "desc",
                category = "electronics",
                imageUri = "content://test/image",
            ),
        )

        assertTrue(result.isFailure)
    }
}
