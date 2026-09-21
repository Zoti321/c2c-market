package com.zoti321.c2cmarket.data.local

import android.content.Context
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class DatabaseMigrationTest {

    @Test
    fun migrate8To9_addsV4FeatureColumns() {
        openDatabase(version = 8, onCreate = { db ->
            createV8Conversation(db)
            createV8Listing(db)
            createV8Order(db)
            db.execSQL(
                """
                INSERT INTO conversations (
                    id, productId, productTitle, productImageUrl, sellerId, sellerDisplayName,
                    buyerId, lastMessagePreview, lastMessageAt, unreadCount, createdAt
                ) VALUES (1, 1, 'P', 'img', 'seller', '卖家', 'guest', '', 1, 0, 1)
                """.trimIndent(),
            )
            db.execSQL(
                """
                INSERT INTO listings (
                    id, catalogId, title, price, description, category, imageUri,
                    sellerId, createdAt, updatedAt
                ) VALUES (1, -1, 'L', 1.0, 'd', 'electronics', 'uri', 'guest', 1, 1)
                """.trimIndent(),
            )
            db.execSQL(
                """
                INSERT INTO orders (id, guestId, totalAmount, status, createdAt)
                VALUES (1, 'guest', 10.0, 'COMPLETED', 1)
                """.trimIndent(),
            )
        }).use { db ->
            MIGRATION_8_9.migrate(db)

            db.query("PRAGMA table_info(conversations)").use { cursor ->
                val columns = mutableListOf<String>()
                while (cursor.moveToNext()) {
                    columns += cursor.getString(cursor.getColumnIndexOrThrow("name"))
                }
                assertTrue(columns.contains("buyerDisplayName"))
                assertTrue(columns.contains("sellerUnreadCount"))
            }

            db.query("SELECT buyerDisplayName, sellerUnreadCount FROM conversations WHERE id = 1")
                .use { cursor ->
                    assertTrue(cursor.moveToFirst())
                    assertEquals("游客", cursor.getString(0))
                    assertEquals(0, cursor.getInt(1))
                }

            db.query("SELECT status FROM listings WHERE catalogId = -1").use { cursor ->
                assertTrue(cursor.moveToFirst())
                assertEquals("AVAILABLE", cursor.getString(0))
            }

            db.query("SELECT buyerMeetupConfirmed, sellerMeetupConfirmed FROM orders WHERE id = 1")
                .use { cursor ->
                    assertTrue(cursor.moveToFirst())
                    assertEquals(0, cursor.getInt(0))
                    assertEquals(0, cursor.getInt(1))
                }
        }
    }

    @Test
    fun migrate10To11_addsRemoteSyncColumns() {
        openDatabase(version = 10, onCreate = { db ->
            createV10Conversation(db)
            createV10Message(db)
            createV10Order(db)
            db.execSQL(
                """
                INSERT INTO conversations (
                    id, productId, productTitle, productImageUrl, sellerId, sellerDisplayName,
                    buyerId, buyerDisplayName, lastMessagePreview, lastMessageAt, unreadCount,
                    sellerUnreadCount, createdAt
                ) VALUES (1, 1, 'P', 'img', 'seller', '卖家', 'guest', '游客', '', 1, 0, 0, 1)
                """.trimIndent(),
            )
            db.execSQL(
                """
                INSERT INTO messages (
                    id, conversationId, senderId, body, status, sentAt, isRead
                ) VALUES (1, 1, 'guest', 'hi', 'SENT', 1, 1)
                """.trimIndent(),
            )
            db.execSQL(
                """
                INSERT INTO orders (id, guestId, totalAmount, status, createdAt)
                VALUES (1, 'guest', 10.0, 'COMPLETED', 1)
                """.trimIndent(),
            )
        }).use { db ->
            MIGRATION_10_11.migrate(db)

            db.query("PRAGMA table_info(conversations)").use { cursor ->
                val columns = columnNames(cursor)
                assertTrue(columns.contains("remoteId"))
            }
            db.query("PRAGMA table_info(messages)").use { cursor ->
                val columns = columnNames(cursor)
                assertTrue(columns.contains("remoteId"))
                assertTrue(columns.contains("syncState"))
            }
            db.query("PRAGMA table_info(orders)").use { cursor ->
                val columns = columnNames(cursor)
                assertTrue(columns.contains("remoteId"))
                assertTrue(columns.contains("syncState"))
            }
            db.query("SELECT syncState FROM messages WHERE id = 1").use { cursor ->
                assertTrue(cursor.moveToFirst())
                assertEquals("SYNCED", cursor.getString(0))
            }
        }
    }

    @Test
    fun migrate9To10_addsUserIdToCartAndFavorites() {
        openDatabase(version = 9, onCreate = { db ->
            db.execSQL(
                """
                CREATE TABLE cart_items (
                    productId INTEGER NOT NULL PRIMARY KEY,
                    title TEXT NOT NULL,
                    unitPrice REAL NOT NULL,
                    imageUrl TEXT NOT NULL,
                    quantity INTEGER NOT NULL,
                    addedAt INTEGER NOT NULL
                )
                """.trimIndent(),
            )
            db.execSQL(
                """
                CREATE TABLE favorites (
                    productId INTEGER NOT NULL PRIMARY KEY,
                    createdAt INTEGER NOT NULL
                )
                """.trimIndent(),
            )
            db.execSQL(
                """
                INSERT INTO cart_items (productId, title, unitPrice, imageUrl, quantity, addedAt)
                VALUES (1, 'Phone', 9.9, 'img', 1, 1)
                """.trimIndent(),
            )
            db.execSQL("INSERT INTO favorites (productId, createdAt) VALUES (1, 1)")
        }).use { db ->
            MIGRATION_9_10.migrate(db)

            db.query("SELECT userId, productId FROM cart_items").use { cursor ->
                assertTrue(cursor.moveToFirst())
                assertEquals("guest", cursor.getString(0))
                assertEquals(1, cursor.getInt(1))
            }

            db.query("SELECT userId, productId FROM favorites").use { cursor ->
                assertTrue(cursor.moveToFirst())
                assertEquals("guest", cursor.getString(0))
                assertEquals(1, cursor.getInt(1))
            }
        }
    }

    private fun openDatabase(
        version: Int,
        onCreate: (SupportSQLiteDatabase) -> Unit,
    ): SupportSQLiteDatabase {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val config = androidx.sqlite.db.SupportSQLiteOpenHelper.Configuration.builder(context)
            .name("migration-test-$version.db")
            .callback(object : androidx.sqlite.db.SupportSQLiteOpenHelper.Callback(version) {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    onCreate(db)
                }

                override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit
            })
            .build()
        val helper = FrameworkSQLiteOpenHelperFactory().create(config)
        return helper.writableDatabase
    }

    private fun createV8Conversation(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE conversations (
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
    }

    private fun createV8Listing(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE listings (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                catalogId INTEGER NOT NULL,
                title TEXT NOT NULL,
                price REAL NOT NULL,
                description TEXT NOT NULL,
                category TEXT NOT NULL,
                imageUri TEXT NOT NULL,
                sellerId TEXT NOT NULL,
                createdAt INTEGER NOT NULL,
                updatedAt INTEGER NOT NULL
            )
            """.trimIndent(),
        )
    }

    private fun columnNames(cursor: android.database.Cursor): List<String> {
        val columns = mutableListOf<String>()
        while (cursor.moveToNext()) {
            columns += cursor.getString(cursor.getColumnIndexOrThrow("name"))
        }
        return columns
    }

    private fun createV10Conversation(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE conversations (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                productId INTEGER NOT NULL,
                productTitle TEXT NOT NULL,
                productImageUrl TEXT NOT NULL,
                sellerId TEXT NOT NULL,
                sellerDisplayName TEXT NOT NULL,
                buyerId TEXT NOT NULL,
                buyerDisplayName TEXT NOT NULL,
                lastMessagePreview TEXT NOT NULL,
                lastMessageAt INTEGER NOT NULL,
                unreadCount INTEGER NOT NULL,
                sellerUnreadCount INTEGER NOT NULL,
                createdAt INTEGER NOT NULL
            )
            """.trimIndent(),
        )
    }

    private fun createV10Message(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE messages (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                conversationId INTEGER NOT NULL,
                senderId TEXT NOT NULL,
                body TEXT NOT NULL,
                status TEXT NOT NULL,
                sentAt INTEGER,
                isRead INTEGER NOT NULL
            )
            """.trimIndent(),
        )
    }

    private fun createV10Order(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE orders (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                guestId TEXT NOT NULL,
                totalAmount REAL NOT NULL,
                status TEXT NOT NULL,
                createdAt INTEGER NOT NULL
            )
            """.trimIndent(),
        )
    }

    private fun createV8Order(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE orders (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                guestId TEXT NOT NULL,
                totalAmount REAL NOT NULL,
                status TEXT NOT NULL,
                createdAt INTEGER NOT NULL
            )
            """.trimIndent(),
        )
    }
}
