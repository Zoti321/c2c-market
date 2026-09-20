package com.zoti321.c2cmarket.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider

object InMemoryDatabaseFactory {
    fun create(): C2CDatabase =
        Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            C2CDatabase::class.java,
        ).allowMainThreadQueries().build()
}
