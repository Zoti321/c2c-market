package com.zoti321.c2cmarket.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS browse_history (
                productId INTEGER NOT NULL PRIMARY KEY,
                title TEXT NOT NULL,
                imageUrl TEXT NOT NULL,
                price REAL NOT NULL,
                viewedAt INTEGER NOT NULL
            )
            """.trimIndent(),
        )
    }
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS listings (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                catalogId INTEGER NOT NULL,
                title TEXT NOT NULL,
                price REAL NOT NULL,
                description TEXT NOT NULL,
                category TEXT NOT NULL,
                imageUri TEXT NOT NULL,
                createdAt INTEGER NOT NULL,
                updatedAt INTEGER NOT NULL
            )
            """.trimIndent(),
        )
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_listings_catalogId ON listings(catalogId)")
    }
}

val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS addresses (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                receiverName TEXT NOT NULL,
                phone TEXT NOT NULL,
                region TEXT NOT NULL,
                detail TEXT NOT NULL,
                isDefault INTEGER NOT NULL,
                updatedAt INTEGER NOT NULL
            )
            """.trimIndent(),
        )
        db.execSQL("ALTER TABLE orders ADD COLUMN shippingReceiverName TEXT")
        db.execSQL("ALTER TABLE orders ADD COLUMN shippingPhone TEXT")
        db.execSQL("ALTER TABLE orders ADD COLUMN shippingAddress TEXT")
    }
}

val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS conversations (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                productId INTEGER NOT NULL,
                productTitle TEXT NOT NULL,
                productImageUrl TEXT NOT NULL,
                sellerId TEXT NOT NULL,
                sellerDisplayName TEXT NOT NULL,
                buyerId TEXT NOT NULL,
                lastMessagePreview TEXT NOT NULL,
                lastMessageAt INTEGER NOT NULL,
                unreadCount INTEGER NOT NULL,
                createdAt INTEGER NOT NULL
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            CREATE UNIQUE INDEX IF NOT EXISTS index_conversations_buyerId_sellerId_productId
            ON conversations(buyerId, sellerId, productId)
            """.trimIndent(),
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS messages (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                conversationId INTEGER NOT NULL,
                senderId TEXT NOT NULL,
                body TEXT NOT NULL,
                status TEXT NOT NULL,
                sentAt INTEGER,
                isRead INTEGER NOT NULL,
                FOREIGN KEY(conversationId) REFERENCES conversations(id) ON DELETE CASCADE
            )
            """.trimIndent(),
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS index_messages_conversationId_status ON messages(conversationId, status)",
        )
    }
}

val MIGRATION_7_8 = object : Migration(7, 8) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE listings ADD COLUMN meetupLocation TEXT")
    }
}

val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "ALTER TABLE listings ADD COLUMN sellerId TEXT NOT NULL DEFAULT 'guest'",
        )
    }
}

val MIGRATION_8_9 = object : Migration(8, 9) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "ALTER TABLE conversations ADD COLUMN buyerDisplayName TEXT NOT NULL DEFAULT '游客'",
        )
        db.execSQL(
            "ALTER TABLE conversations ADD COLUMN sellerUnreadCount INTEGER NOT NULL DEFAULT 0",
        )
    }
}

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
