package com.zoti321.c2cmarket.data.repository

import com.zoti321.c2cmarket.domain.error.ProductNotFoundException
import com.zoti321.c2cmarket.data.remote.FakeStoreApi
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.create

class FakeStoreProductRepositoryTest {

    private lateinit var server: MockWebServer
    private lateinit var repository: FakeStoreProductRepository

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        val json = Json { ignoreUnknownKeys = true; coerceInputValues = true }
        val retrofit = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .client(OkHttpClient.Builder().build())
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
        val api: FakeStoreApi = retrofit.create()
        repository = FakeStoreProductRepository(api, FakeListingRepository())
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun getProduct_returnsProduct_whenValidId() = runTest {
        server.enqueue(
            MockResponse().setBody(
                """
                {
                  "id": 1,
                  "title": "Test Product",
                  "price": 9.99,
                  "description": "Desc",
                  "category": "electronics",
                  "image": "https://example.com/img.png",
                  "rating": { "rate": 4.5, "count": 10 }
                }
                """.trimIndent(),
            ),
        )

        val result = repository.getProduct(1)

        assertTrue(result.isSuccess)
        assertEquals("Test Product", result.getOrThrow().title)
        assertEquals(9.99, result.getOrThrow().price, 0.001)
    }

    @Test
    fun getProduct_returnsNotFound_whenEmptyBody() = runTest {
        server.enqueue(MockResponse().setBody(""))

        val result = repository.getProduct(99999)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is ProductNotFoundException)
    }

    @Test
    fun getCategories_returnsList() = runTest {
        server.enqueue(
            MockResponse().setBody("""["electronics","jewelery"]"""),
        )

        val result = repository.getCategories()

        assertTrue(result.isSuccess)
        assertEquals(listOf("electronics", "jewelery"), result.getOrThrow())
    }
}
