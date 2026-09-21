package com.zoti321.c2cmarket.data.sync

import com.zoti321.c2cmarket.data.firebase.FirebaseAuthGateway
import com.zoti321.c2cmarket.data.firebase.FcmTokenRepository
import com.zoti321.c2cmarket.data.firebase.ListingStatusTransitions
import com.zoti321.c2cmarket.data.firebase.OrderStatusTransitions
import com.zoti321.c2cmarket.data.local.dao.ConversationDao
import com.zoti321.c2cmarket.data.local.dao.ListingDao
import com.zoti321.c2cmarket.data.local.dao.MessageDao
import com.zoti321.c2cmarket.data.local.dao.OrderDao
import com.zoti321.c2cmarket.data.local.entity.ConversationEntity
import com.zoti321.c2cmarket.data.local.entity.ListingEntity
import com.zoti321.c2cmarket.data.local.entity.MessageEntity
import com.zoti321.c2cmarket.data.local.entity.OrderEntity
import com.zoti321.c2cmarket.data.local.entity.OrderLineItemEntity
import com.zoti321.c2cmarket.data.local.entity.SyncStateValues
import com.zoti321.c2cmarket.data.mapper.toEntityValue
import com.zoti321.c2cmarket.di.ApplicationScope
import com.zoti321.c2cmarket.di.IoDispatcher
import com.zoti321.c2cmarket.domain.datasource.ChatRemoteDataSource
import com.zoti321.c2cmarket.domain.datasource.ListingRemoteDataSource
import com.zoti321.c2cmarket.domain.datasource.OrderRemoteDataSource
import com.zoti321.c2cmarket.domain.datasource.RemoteConversation
import com.zoti321.c2cmarket.domain.datasource.RemoteListing
import com.zoti321.c2cmarket.domain.datasource.RemoteOrder
import com.zoti321.c2cmarket.domain.model.AuthState
import com.zoti321.c2cmarket.domain.model.ListingStatus
import com.zoti321.c2cmarket.domain.model.MessageStatus
import com.zoti321.c2cmarket.domain.model.OrderStatus
import com.zoti321.c2cmarket.domain.repository.AuthRepository
import com.google.firebase.messaging.FirebaseMessaging
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

@Singleton
class RemoteSyncCoordinator @Inject constructor(
    private val authRepository: AuthRepository,
    private val firebaseAuthGateway: FirebaseAuthGateway,
    private val chatRemote: ChatRemoteDataSource,
    private val listingRemote: ListingRemoteDataSource,
    private val orderRemote: OrderRemoteDataSource,
    private val conversationDao: ConversationDao,
    private val messageDao: MessageDao,
    private val listingDao: ListingDao,
    private val orderDao: OrderDao,
    private val fcmTokenRepository: FcmTokenRepository,
    @ApplicationScope private val applicationScope: CoroutineScope,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : RemoteSyncGateway {
    private var syncJob: Job? = null
    private var activeMessagesJob: Job? = null

    override fun start() {
        applicationScope.launch(ioDispatcher) {
            authRepository.observeAuthState()
                .map { state ->
                    when (state) {
                        AuthState.Guest -> null
                        is AuthState.SignedIn -> state.profile.userId
                    }
                }
                .distinctUntilChanged()
                .collectLatest { userId ->
                    syncJob?.cancel()
                    activeMessagesJob?.cancel()
                    if (userId == null || !firebaseAuthGateway.isSignedIn()) return@collectLatest
                    syncJob = launch { observeRemoteData(userId) }
                    registerFcmToken()
                    listingRemote.migratePendingImages(userId)
                }
        }
    }

    override fun observeActiveConversationMessages(conversationRemoteId: String) {
        activeMessagesJob?.cancel()
        if (!firebaseAuthGateway.isSignedIn()) return
        activeMessagesJob = applicationScope.launch(ioDispatcher) {
            chatRemote.observeRemoteMessages(conversationRemoteId).collect { messages ->
                messages.forEach { mergeRemoteMessage(conversationRemoteId, it) }
            }
        }
    }

    override fun stopActiveConversationMessages() {
        activeMessagesJob?.cancel()
        activeMessagesJob = null
    }

    private suspend fun observeRemoteData(userId: String) {
        coroutineScope {
            launch { chatRemote.observeRemoteConversations(userId).collect { mergeRemoteConversations(it) } }
            launch { listingRemote.observeRemoteListingsAsSeller(userId).collect { mergeRemoteListings(it) } }
            launch { listingRemote.observeAvailableListings().collect { mergeRemoteListings(it) } }
            launch { orderRemote.observeRemoteOrders(userId).collect { mergeRemoteOrders(it) } }
        }
    }

    private suspend fun registerFcmToken() {
        runCatching {
            val token = FirebaseMessaging.getInstance().token.await()
            fcmTokenRepository.upsertToken(token)
        }
    }

    override suspend fun unregisterFcmToken() {
        runCatching {
            val token = FirebaseMessaging.getInstance().token.await()
            fcmTokenRepository.deleteToken(token)
        }
    }

    private suspend fun mergeRemoteConversations(remote: List<RemoteConversation>) {
        withContext(ioDispatcher) {
            remote.forEach { item ->
                val existing = conversationDao.findByRemoteId(item.remoteId)
                    ?: conversationDao.findByUniqueKey(item.buyerId, item.sellerId, item.productId)
                if (existing == null) {
                    conversationDao.insert(
                        ConversationEntity(
                            productId = item.productId,
                            productTitle = item.productTitle,
                            productImageUrl = item.productImageUrl,
                            sellerId = item.sellerId,
                            sellerDisplayName = item.sellerDisplayName,
                            buyerId = item.buyerId,
                            buyerDisplayName = item.buyerDisplayName,
                            lastMessagePreview = item.lastMessagePreview,
                            lastMessageAt = item.lastMessageAt,
                            unreadCount = item.unreadCount,
                            sellerUnreadCount = item.sellerUnreadCount,
                            createdAt = item.createdAt,
                            remoteId = item.remoteId,
                        ),
                    )
                } else {
                    conversationDao.update(
                        existing.copy(
                            remoteId = item.remoteId,
                            productTitle = item.productTitle,
                            productImageUrl = item.productImageUrl,
                            buyerDisplayName = item.buyerDisplayName,
                            sellerDisplayName = item.sellerDisplayName,
                        ),
                    )
                    conversationDao.mergePreviewIfNewer(
                        id = existing.id,
                        preview = item.lastMessagePreview,
                        lastMessageAt = item.lastMessageAt,
                        unreadCount = item.unreadCount,
                        sellerUnreadCount = item.sellerUnreadCount,
                    )
                }
            }
        }
    }

    private suspend fun mergeRemoteMessage(conversationRemoteId: String, remote: com.zoti321.c2cmarket.domain.datasource.RemoteMessage) {
        withContext(ioDispatcher) {
            if (messageDao.findByRemoteId(remote.remoteId) != null) return@withContext
            val conversation = conversationDao.findByRemoteId(conversationRemoteId) ?: return@withContext
            messageDao.insert(
                MessageEntity(
                    conversationId = conversation.id,
                    senderId = remote.senderId,
                    body = remote.body,
                    status = MessageStatus.SENT.toEntityValue(),
                    sentAt = remote.sentAt,
                    isRead = remote.isRead,
                    remoteId = remote.remoteId,
                    syncState = SyncStateValues.SYNCED,
                ),
            )
            conversationDao.mergePreviewIfNewer(
                id = conversation.id,
                preview = remote.body,
                lastMessageAt = remote.sentAt,
                unreadCount = conversation.unreadCount,
                sellerUnreadCount = conversation.sellerUnreadCount,
            )
        }
    }

    private suspend fun mergeRemoteListings(remote: List<RemoteListing>) {
        withContext(ioDispatcher) {
            remote.forEach { item ->
                val existing = listingDao.getByCatalogId(item.catalogId)
                if (existing == null) {
                    listingDao.insert(item.toEntity())
                } else {
                    val currentStatus = runCatching { ListingStatus.valueOf(existing.status) }
                        .getOrDefault(ListingStatus.AVAILABLE)
                    val mergedStatus = if (ListingStatusTransitions.canTransition(currentStatus, item.status)) {
                        item.status.name
                    } else {
                        existing.status
                    }
                    val mergedUpdatedAt = maxOf(existing.updatedAt, item.updatedAt)
                    listingDao.update(
                        existing.copy(
                            title = if (item.updatedAt >= existing.updatedAt) item.title else existing.title,
                            price = if (item.updatedAt >= existing.updatedAt) item.price else existing.price,
                            description = if (item.updatedAt >= existing.updatedAt) item.description else existing.description,
                            category = if (item.updatedAt >= existing.updatedAt) item.category else existing.category,
                            imageUri = if (item.updatedAt >= existing.updatedAt) item.imageUrl else existing.imageUri,
                            meetupLocation = if (item.updatedAt >= existing.updatedAt) item.meetupLocation else existing.meetupLocation,
                            status = mergedStatus,
                            updatedAt = mergedUpdatedAt,
                        ),
                    )
                }
            }
        }
    }

    private suspend fun mergeRemoteOrders(remote: List<RemoteOrder>) {
        withContext(ioDispatcher) {
            remote.forEach { item ->
                val existing = orderDao.findByRemoteId(item.remoteId)
                if (existing == null) {
                    val orderId = orderDao.insertOrder(
                        OrderEntity(
                            guestId = item.buyerId,
                            totalAmount = item.totalAmount,
                            status = item.status.name,
                            createdAt = item.createdAt,
                            meetupLocation = item.meetupLocation,
                            buyerMeetupConfirmed = item.buyerMeetupConfirmed,
                            sellerMeetupConfirmed = item.sellerMeetupConfirmed,
                            remoteId = item.remoteId,
                            syncState = SyncStateValues.SYNCED,
                        ),
                    )
                    orderDao.insertLineItems(
                        item.lineItems.map { line ->
                            OrderLineItemEntity(
                                orderId = orderId,
                                productId = line.productId,
                                title = line.title,
                                unitPrice = line.unitPrice,
                                quantity = line.quantity,
                                imageUrl = line.imageUrl,
                            )
                        },
                    )
                } else {
                    val currentStatus = runCatching { OrderStatus.valueOf(existing.status) }
                        .getOrDefault(OrderStatus.PENDING)
                    val mergedStatus = if (OrderStatusTransitions.canTransition(currentStatus, item.status)) {
                        item.status.name
                    } else {
                        existing.status
                    }
                    orderDao.updateRemoteSync(existing.id, item.remoteId, SyncStateValues.SYNCED)
                    orderDao.updateStatus(existing.id, mergedStatus)
                }
            }
        }
    }

    private fun RemoteListing.toEntity(): ListingEntity = ListingEntity(
        catalogId = catalogId,
        title = title,
        price = price,
        description = description,
        category = category,
        imageUri = imageUrl,
        sellerId = sellerId,
        status = status.name,
        meetupLocation = meetupLocation,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}
