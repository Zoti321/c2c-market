package com.zoti321.c2cmarket.data.local

import com.zoti321.c2cmarket.data.local.entity.ConversationEntity
import com.zoti321.c2cmarket.data.local.entity.MessageEntity
import com.zoti321.c2cmarket.domain.GuestSession
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class MessageDaoTest {

    private lateinit var database: C2CDatabase

    @Before
    fun setUp() {
        database = InMemoryDatabaseFactory.create()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertMessage_withValidConversationId_persistsRow() = runTest {
        val conversationId = insertSampleConversation()
        val messageDao = database.messageDao()

        val messageId = messageDao.insert(
            MessageEntity(
                conversationId = conversationId,
                senderId = GuestSession.GUEST_ID,
                body = "hello",
                status = "SENT",
                sentAt = 100L,
                isRead = true,
            ),
        )

        assertTrue(messageId > 0)
        val sent = messageDao.observeSent(conversationId).first()
        assertEquals(1, sent.size)
        assertEquals("hello", sent.first().body)
    }

    @Test
    fun deleteConversation_cascadesMessages() = runTest {
        val conversationDao = database.conversationDao()
        val messageDao = database.messageDao()
        val conversationId = insertSampleConversation()
        messageDao.insert(
            MessageEntity(
                conversationId = conversationId,
                senderId = GuestSession.GUEST_ID,
                body = "draft",
                status = "DRAFT",
                sentAt = null,
                isRead = true,
            ),
        )
        messageDao.insert(
            MessageEntity(
                conversationId = conversationId,
                senderId = GuestSession.GUEST_ID,
                body = "sent",
                status = "SENT",
                sentAt = 200L,
                isRead = true,
            ),
        )

        conversationDao.getById(conversationId)?.let { entity ->
            database.openHelper.writableDatabase.execSQL(
                "DELETE FROM conversations WHERE id = ?",
                arrayOf(entity.id),
            )
        }

        assertNull(messageDao.getDraft(conversationId))
        assertTrue(messageDao.observeSent(conversationId).first().isEmpty())
    }

    @Test
    fun draftAndSentMessages_canCoexist() = runTest {
        val conversationId = insertSampleConversation()
        val messageDao = database.messageDao()
        messageDao.insert(
            MessageEntity(
                conversationId = conversationId,
                senderId = GuestSession.GUEST_ID,
                body = "typing…",
                status = "DRAFT",
                sentAt = null,
                isRead = true,
            ),
        )
        messageDao.insert(
            MessageEntity(
                conversationId = conversationId,
                senderId = "seller-1",
                body = "previous reply",
                status = "SENT",
                sentAt = 300L,
                isRead = false,
            ),
        )

        assertEquals("typing…", messageDao.getDraft(conversationId)?.body)
        val sent = messageDao.observeSent(conversationId).first()
        assertEquals(1, sent.size)
        assertEquals("previous reply", sent.first().body)
    }

    private suspend fun insertSampleConversation(): Long {
        return database.conversationDao().insert(
            ConversationEntity(
                productId = 1,
                productTitle = "Test Product",
                productImageUrl = "https://example.com/img.jpg",
                sellerId = "seller-1",
                sellerDisplayName = "卖家",
                buyerId = GuestSession.GUEST_ID,
                lastMessagePreview = "",
                lastMessageAt = 1L,
                unreadCount = 0,
                createdAt = 1L,
            ),
        )
    }
}
