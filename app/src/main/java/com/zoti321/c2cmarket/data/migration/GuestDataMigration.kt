package com.zoti321.c2cmarket.data.migration

fun interface GuestDataMigration {
    suspend fun migrateGuestDataTo(newUserId: String)
}
