package com.zoti321.c2cmarket.ui.common

import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FlowUiStateTest {

    @Test
    fun asUiStateFlow_emitsLoadingThenSuccess() = runTest {
        val emissions = flow { emit(listOf(1, 2)) }
            .asUiStateFlow()
            .toList()

        assertEquals(UiState.Loading, emissions[0])
        assertTrue(emissions[1] is UiState.Success)
        assertEquals(listOf(1, 2), (emissions[1] as UiState.Success).data)
    }

    @Test
    fun asUiStateFlow_emitsErrorOnFailure() = runTest {
        val emissions = flow<List<Int>> {
            throw IllegalStateException("db error")
        }.asUiStateFlow().toList()

        assertEquals(UiState.Loading, emissions[0])
        assertTrue(emissions[1] is UiState.Error)
    }
}
