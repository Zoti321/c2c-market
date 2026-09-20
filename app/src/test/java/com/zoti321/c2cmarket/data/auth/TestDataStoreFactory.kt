package com.zoti321.c2cmarket.data.auth

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.test.UnconfinedTestDispatcher

object TestDataStoreFactory {
    fun create(): DataStore<Preferences> {
        val file = File.createTempFile("test_auth", ".preferences_pb")
        return PreferenceDataStoreFactory.create(
            scope = CoroutineScope(UnconfinedTestDispatcher() + SupervisorJob()),
            produceFile = { file },
        )
    }
}
