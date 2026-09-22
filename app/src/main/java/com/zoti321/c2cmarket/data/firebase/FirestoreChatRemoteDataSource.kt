package com.zoti321.c2cmarket.data.firebase

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.zoti321.c2cmarket.domain.datasource.ChatRemoteDataSource
import com.zoti321.c2cmarket.domain.datasource.RemoteConversation
import com.zoti321.c2cmarket.domain.datasource.RemoteMessage
import com.zoti321.c2cmarket.domain.model.Conversation
import com.zoti321.c2cmarket.domain.model.Message
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

@Singleton
class FirestoreChatRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
) : ChatRemoteDataSource {

    override suspend fun ensureConversation(conversation: Conversation, remoteId: String): Result<Unit> =
        runCatching {
            val doc = firestore.collection(COLLECTION_CONVERSATIONS).document(remoteId)
            if (doc.get().await().exists()) return@runCatching
            doc.set(
                mapOf(
                    FIELD_BUYER_ID to conversation.buyerId,
                    FIELD_SELLER_ID to conversation.sellerId,
                    FIELD_PRODUCT_ID to conversation.productId,
                    FIELD_PRODUCT_TITLE to conversation.productTitle,
                    FIELD_PRODUCT_IMAGE_URL to conversation.productImageUrl,
                    FIELD_BUYER_DISPLAY_NAME to conversation.buyerDisplayName,
                    FIELD_SELLER_DISPLAY_NAME to conversation.sellerDisplayName,
                    FIELD_LAST_MESSAGE_PREVIEW to conversation.lastMessagePreview,
                    FIELD_LAST_MESSAGE_AT to conversation.lastMessageAt,
                    FIELD_UNREAD_COUNT to conversation.unreadCount,
                    FIELD_SELLER_UNREAD_COUNT to conversation.sellerUnreadCount,
                    FIELD_CREATED_AT to conversation.createdAt,
                ),
            ).await()
        }

    override suspend fun uploadMessage(
        conversationRemoteId: String,
        message: Message,
        remoteId: String,
    ): Result<Unit> = runCatching {
        val sentAt = message.sentAt ?: System.currentTimeMillis()
        firestore.collection(COLLECTION_CONVERSATIONS)
            .document(conversationRemoteId)
            .collection(SUBCOLLECTION_MESSAGES)
            .document(remoteId)
            .set(
                mapOf(
                    FIELD_SENDER_ID to message.senderId,
                    FIELD_BODY to message.body,
                    FIELD_SENT_AT to sentAt,
                    FIELD_IS_READ to message.isRead,
                ),
            )
            .await()
        firestore.collection(COLLECTION_CONVERSATIONS)
            .document(conversationRemoteId)
            .update(
                mapOf(
                    FIELD_LAST_MESSAGE_PREVIEW to message.body,
                    FIELD_LAST_MESSAGE_AT to sentAt,
                ),
            )
            .await()
    }

    override fun observeRemoteConversations(userId: String): Flow<List<RemoteConversation>> =
        callbackFlow {
            val buyerMap = mutableMapOf<String, RemoteConversation>()
            val sellerMap = mutableMapOf<String, RemoteConversation>()

            fun emitMerged() {
                val merged = (buyerMap.values + sellerMap.values)
                    .distinctBy { it.remoteId }
                trySend(merged)
            }

            val buyerRegistration = firestore.collection(COLLECTION_CONVERSATIONS)
                .whereEqualTo(FIELD_BUYER_ID, userId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) return@addSnapshotListener
                    buyerMap.clear()
                    snapshot?.documents.orEmpty().forEach { doc ->
                        doc.toRemoteConversation()?.let { buyerMap[it.remoteId] = it }
                    }
                    emitMerged()
                }
            val sellerRegistration = firestore.collection(COLLECTION_CONVERSATIONS)
                .whereEqualTo(FIELD_SELLER_ID, userId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) return@addSnapshotListener
                    sellerMap.clear()
                    snapshot?.documents.orEmpty().forEach { doc ->
                        doc.toRemoteConversation()?.let { sellerMap[it.remoteId] = it }
                    }
                    emitMerged()
                }
            awaitClose {
                buyerRegistration.remove()
                sellerRegistration.remove()
            }
        }

    override fun observeRemoteMessages(conversationRemoteId: String): Flow<List<RemoteMessage>> =
        callbackFlow {
            val registration = firestore.collection(COLLECTION_CONVERSATIONS)
                .document(conversationRemoteId)
                .collection(SUBCOLLECTION_MESSAGES)
                .orderBy(FIELD_SENT_AT, Query.Direction.ASCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) return@addSnapshotListener
                    val items = snapshot?.documents.orEmpty().mapNotNull { doc ->
                        val remoteId = doc.id
                        val senderId = doc.getString(FIELD_SENDER_ID) ?: return@mapNotNull null
                        val body = doc.getString(FIELD_BODY) ?: return@mapNotNull null
                        val sentAt = doc.getLong(FIELD_SENT_AT) ?: return@mapNotNull null
                        val isRead = doc.getBoolean(FIELD_IS_READ) ?: false
                        RemoteMessage(
                            remoteId = remoteId,
                            senderId = senderId,
                            body = body,
                            sentAt = sentAt,
                            isRead = isRead,
                        )
                    }
                    trySend(items)
                }
            awaitClose { registration.remove() }
        }

    override suspend fun retryPendingUploads() = Unit

    private fun com.google.firebase.firestore.DocumentSnapshot.toRemoteConversation(): RemoteConversation? {
        val remoteId = id
        val buyerId = getString(FIELD_BUYER_ID)
        val sellerId = getString(FIELD_SELLER_ID)
        val productId = getLong(FIELD_PRODUCT_ID)?.toInt()
        if (buyerId == null || sellerId == null || productId == null) {
            return null
        }
        return RemoteConversation(
            remoteId = remoteId,
            buyerId = buyerId,
            sellerId = sellerId,
            productId = productId,
            productTitle = getString(FIELD_PRODUCT_TITLE).orEmpty(),
            productImageUrl = getString(FIELD_PRODUCT_IMAGE_URL).orEmpty(),
            buyerDisplayName = getString(FIELD_BUYER_DISPLAY_NAME).orEmpty(),
            sellerDisplayName = getString(FIELD_SELLER_DISPLAY_NAME).orEmpty(),
            lastMessagePreview = getString(FIELD_LAST_MESSAGE_PREVIEW).orEmpty(),
            lastMessageAt = getLong(FIELD_LAST_MESSAGE_AT) ?: 0L,
            unreadCount = getLong(FIELD_UNREAD_COUNT)?.toInt() ?: 0,
            sellerUnreadCount = getLong(FIELD_SELLER_UNREAD_COUNT)?.toInt() ?: 0,
            createdAt = getLong(FIELD_CREATED_AT) ?: 0L,
        )
    }

    companion object {
        const val COLLECTION_CONVERSATIONS = "conversations"
        const val SUBCOLLECTION_MESSAGES = "messages"
        const val FIELD_BUYER_ID = "buyerId"
        const val FIELD_SELLER_ID = "sellerId"
        const val FIELD_PRODUCT_ID = "productId"
        const val FIELD_PRODUCT_TITLE = "productTitle"
        const val FIELD_PRODUCT_IMAGE_URL = "productImageUrl"
        const val FIELD_BUYER_DISPLAY_NAME = "buyerDisplayName"
        const val FIELD_SELLER_DISPLAY_NAME = "sellerDisplayName"
        const val FIELD_LAST_MESSAGE_PREVIEW = "lastMessagePreview"
        const val FIELD_LAST_MESSAGE_AT = "lastMessageAt"
        const val FIELD_UNREAD_COUNT = "unreadCount"
        const val FIELD_SELLER_UNREAD_COUNT = "sellerUnreadCount"
        const val FIELD_CREATED_AT = "createdAt"
        const val FIELD_SENDER_ID = "senderId"
        const val FIELD_BODY = "body"
        const val FIELD_SENT_AT = "sentAt"
        const val FIELD_IS_READ = "isRead"
    }
}
