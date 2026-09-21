package com.zoti321.c2cmarket.data.sync

import com.zoti321.c2cmarket.data.firebase.FirebaseAuthGateway
import com.zoti321.c2cmarket.data.firebase.FcmTokenRepository
import com.zoti321.c2cmarket.data.sync.merger.ConversationRemoteMerger
import com.zoti321.c2cmarket.data.sync.merger.ListingRemoteMerger
import com.zoti321.c2cmarket.data.sync.merger.MessageRemoteMerger
import com.zoti321.c2cmarket.data.sync.merger.OrderRemoteMerger
import com.zoti321.c2cmarket.di.ApplicationScope
import com.zoti321.c2cmarket.di.IoDispatcher
import com.zoti321.c2cmarket.domain.datasource.ChatRemoteDataSource
import com.zoti321.c2cmarket.domain.datasource.ListingRemoteDataSource
import com.zoti321.c2cmarket.domain.datasource.OrderRemoteDataSource
import com.zoti321.c2cmarket.domain.model.AuthState
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
    private val conversationMerger: ConversationRemoteMerger,
    private val messageMerger: MessageRemoteMerger,
    private val listingMerger: ListingRemoteMerger,
    private val orderMerger: OrderRemoteMerger,
    private val listingImageMigrator: ListingImageMigrator,
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
                    listingImageMigrator.migrateContentUrisForSeller(userId)
                }
        }
    }

    override fun observeActiveConversationMessages(conversationRemoteId: String) {
        activeMessagesJob?.cancel()
        if (!firebaseAuthGateway.isSignedIn()) return
        activeMessagesJob = applicationScope.launch(ioDispatcher) {
            chatRemote.observeRemoteMessages(conversationRemoteId).collect { messages ->
                withContext(ioDispatcher) {
                    messages.forEach { messageMerger.merge(conversationRemoteId, it) }
                }
            }
        }
    }

    override fun stopActiveConversationMessages() {
        activeMessagesJob?.cancel()
        activeMessagesJob = null
    }

    private suspend fun observeRemoteData(userId: String) {
        coroutineScope {
            launch {
                chatRemote.observeRemoteConversations(userId).collect { remote ->
                    withContext(ioDispatcher) { conversationMerger.mergeAll(remote) }
                }
            }
            launch {
                listingRemote.observeRemoteListingsAsSeller(userId).collect { remote ->
                    withContext(ioDispatcher) { listingMerger.mergeAll(remote) }
                }
            }
            launch {
                listingRemote.observeAvailableListings().collect { remote ->
                    withContext(ioDispatcher) { listingMerger.mergeAll(remote) }
                }
            }
            launch {
                orderRemote.observeRemoteOrders(userId).collect { remote ->
                    withContext(ioDispatcher) { orderMerger.mergeAll(remote) }
                }
            }
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
}
