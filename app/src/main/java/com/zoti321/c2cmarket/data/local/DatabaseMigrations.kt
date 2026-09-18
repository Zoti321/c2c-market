package com.zoti321.c2cmarket.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS orders (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                guestId TEXT NOT NULL,
                totalAmount REAL NOT NULL,
                status TEXT NOT NULL,
                createdAt INTEGER NOT NULL
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS order_line_items (
                lineId INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                orderId INTEGER NOT NULL,
                productId INTEGER NOT NULL,
                title TEXT NOT NULL,
                unitPrice REAL NOT NULL,
                quantity INTEGER NOT NULL,
                imageUrl TEXT NOT NULL,
                FOREIGN KEY(orderId) REFERENCES orders(id) ON DELETE CASCADE
            )
            """.trimIndent(),
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS index_order_line_items_orderId ON order_line_items(orderId)",
        )
    }
}
