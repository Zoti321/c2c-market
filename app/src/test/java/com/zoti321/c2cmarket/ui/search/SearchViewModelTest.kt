package com.zoti321.c2cmarket.ui.search

import app.cash.turbine.test
import com.zoti321.c2cmarket.data.repository.FakeListingRepository
import com.zoti321.c2cmarket.data.repository.FakeStoreProductRepository
import com.zoti321.c2cmarket.data.remote.FakeStoreApi
import com.zoti321.c2cmarket.domain.model.SearchResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
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

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private lateinit var server: MockWebServer

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        server = MockWebServer()
        server.start()
    }

    @After
    fun tearDown() {
        server.shutdown()
        Dispatchers.resetMain()
    }

    @Test
    fun emptyQuery_emitsIdle() = runTest {
        val viewModel = createViewModel()
        viewModel.searchResult.test {
            assertEquals(SearchResult.Idle, awaitItem())
            viewModel.onQueryChange("")
            advanceTimeBy(400)
            expectNoEvents()
        }
    }

    @Test
    fun queryWithMatch_emitsSuccess() = runTest {
        server.enqueue(
            MockResponse().setBody(
                """
                [
                  {
                    "id": 1,
                    "title": "Phone Case",
                    "price": 9.99,
                    "description": "Desc",
                    "category": "electronics",
                    "image": "https://example.com/img.png",
                    "rating": { "rate": 4.5, "count": 10 }
                  }
                ]
                """.trimIndent(),
            ),
        )
        val viewModel = createViewModel()
        viewModel.searchResult.test {
            assertEquals(SearchResult.Idle, awaitItem())
            viewModel.onQueryChange("phone")
            advanceTimeBy(400)
            assertEquals(SearchResult.Loading, awaitItem())
            val result = awaitItem()
            assertTrue(result is SearchResult.Success)
            assertEquals(1, (result as SearchResult.Success).products.size)
        }
    }

    private fun createViewModel(): SearchViewModel {
        val json = Json { ignoreUnknownKeys = true; coerceInputValues = true }
        val retrofit = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .client(OkHttpClient.Builder().build())
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
        val api: FakeStoreApi = retrofit.create()
        val repository = FakeStoreProductRepository(api, FakeListingRepository())
        return SearchViewModel(repository)
    }
}
