package com.zoti321.c2cmarket.data.repository

import android.net.Uri
import com.zoti321.c2cmarket.data.firebase.FirebaseAuthGateway
import com.zoti321.c2cmarket.data.sync.RemoteSyncGateway
import dagger.Lazy
import com.zoti321.c2cmarket.data.local.dao.CartDao
import com.zoti321.c2cmarket.data.local.dao.OrderDao
import com.zoti321.c2cmarket.data.local.entity.CartItemEntity
import com.zoti321.c2cmarket.data.local.entity.ListingEntity
import com.zoti321.c2cmarket.data.local.entity.OrderEntity
import com.zoti321.c2cmarket.data.local.entity.OrderLineItemEntity
import com.zoti321.c2cmarket.domain.repository.ListingRepository
import com.zoti321.c2cmarket.domain.scheduler.OrderNotificationKind
import com.zoti321.c2cmarket.domain.scheduler.OrderNotificationScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import com.zoti321.c2cmarket.domain.datasource.ChatRemoteDataSource
import com.zoti321.c2cmarket.domain.datasource.ListingRemoteDataSource
import com.zoti321.c2cmarket.domain.datasource.OrderRemoteDataSource
import com.zoti321.c2cmarket.domain.datasource.OrderSyncExtras
import com.zoti321.c2cmarket.domain.datasource.RemoteConversation
import com.zoti321.c2cmarket.domain.datasource.RemoteListing
import com.zoti321.c2cmarket.domain.datasource.RemoteMessage
import com.zoti321.c2cmarket.domain.datasource.RemoteOrder
import com.zoti321.c2cmarket.domain.model.Conversation
import com.zoti321.c2cmarket.domain.model.ListingStatus
import com.zoti321.c2cmarket.domain.model.Message
import com.zoti321.c2cmarket.domain.model.Order
import com.zoti321.c2cmarket.domain.model.OrderStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeFirebaseAuthGateway(
    signedIn: Boolean = false,
    uid: String? = if (signedIn) "firebase-uid" else null,
) : FirebaseAuthGateway {
    private var signedIn: Boolean = signedIn
    private var uid: String? = uid
    override suspend fun signInWithGoogleIdToken(idToken: String): Result<String> {
        signedIn = true
        uid = "firebase-uid"
        return Result.success(uid!!)
    }

    override suspend fun signOut() {
        signedIn = false
        uid = null
    }

    override fun currentFirebaseUid(): String? = uid

    override fun isSignedIn(): Boolean = signedIn
}

fun lazyRemoteSync(gateway: RemoteSyncGateway = FakeRemoteSyncGateway()): Lazy<RemoteSyncGateway> =
    Lazy { gateway }

class FakeRemoteSyncGateway : RemoteSyncGateway {
    override fun start() = Unit
    override fun observeActiveConversationMessages(conversationRemoteId: String) = Unit
    override fun stopActiveConversationMessages() = Unit
    override suspend fun unregisterFcmToken() = Unit
}

class FakeChatRemoteDataSource : ChatRemoteDataSource {
    val uploadedMessages = mutableListOf<Triple<String, Message, String>>()
    var uploadShouldFail = false

    override suspend fun ensureConversation(conversation: Conversation, remoteId: String): Result<Unit> =
        Result.success(Unit)

    override suspend fun uploadMessage(
        conversationRemoteId: String,
        message: Message,
        remoteId: String,
    ): Result<Unit> {
        uploadedMessages += Triple(conversationRemoteId, message, remoteId)
        return if (uploadShouldFail) {
            Result.failure(IllegalStateException("upload failed"))
        } else {
            Result.success(Unit)
        }
    }

    override fun observeRemoteConversations(userId: String): Flow<List<RemoteConversation>> =
        flowOf(emptyList())

    override fun observeRemoteMessages(conversationRemoteId: String): Flow<List<RemoteMessage>> =
        flowOf(emptyList())

    override suspend fun retryPendingUploads() = Unit
}

class FakeListingRemoteDataSource : ListingRemoteDataSource {
    val syncedListings = mutableListOf<ListingEntity>()
    var uploadShouldFail = false

    override suspend fun uploadListingImage(catalogId: Int, localContentUri: Uri): Result<String> =
        if (uploadShouldFail) {
            Result.failure(IllegalStateException("upload failed"))
        } else {
            Result.success("https://firebasestorage.googleapis.com/test/$catalogId.jpg")
        }

    override suspend fun deleteListingImage(catalogId: Int): Result<Unit> = Result.success(Unit)

    override suspend fun migratePendingImages(sellerId: String): Result<Int> = Result.success(0)

    override suspend fun syncListing(listing: ListingEntity): Result<Unit> {
        syncedListings += listing
        return Result.success(Unit)
    }

    override suspend fun syncListingStatus(catalogId: Int, status: ListingStatus): Result<Unit> =
        Result.success(Unit)

    override suspend fun deleteListing(catalogId: Int): Result<Unit> = Result.success(Unit)

    override fun observeRemoteListingsAsSeller(userId: String): Flow<List<RemoteListing>> =
        flowOf(emptyList())

    override fun observeAvailableListings(): Flow<List<RemoteListing>> = flowOf(emptyList())
}

class FakeOrderRemoteDataSource : OrderRemoteDataSource {
    val createdOrders = mutableListOf<Triple<Order, String, String>>()
    val statusUpdates = mutableListOf<Pair<String, OrderStatus>>()

    override suspend fun createOrder(order: Order, remoteId: String, sellerId: String): Result<Unit> {
        createdOrders += Triple(order, remoteId, sellerId)
        return Result.success(Unit)
    }

    override suspend fun syncOrderStatus(
        remoteId: String,
        status: OrderStatus,
        extras: OrderSyncExtras,
    ): Result<Unit> {
        statusUpdates += remoteId to status
        return Result.success(Unit)
    }

    override suspend fun syncMeetupConfirm(
        remoteId: String,
        buyerConfirmed: Boolean,
        sellerConfirmed: Boolean,
    ): Result<Unit> = Result.success(Unit)

    override fun observeRemoteOrders(userId: String): Flow<List<RemoteOrder>> = flowOf(emptyList())

    override suspend fun retryPendingUploads() = Unit
}

class RecordingUserMappingRepository {
    var lastBusinessUserId: String? = null
        private set

    suspend fun upsertBusinessUserId(businessUserId: String): Result<Unit> {
        lastBusinessUserId = businessUserId
        return Result.success(Unit)
    }
}

data class RepositoryTestDeps(
    val dispatcher: kotlinx.coroutines.CoroutineDispatcher,
    val scope: CoroutineScope,
    val listingRemote: FakeListingRemoteDataSource = FakeListingRemoteDataSource(),
    val orderRemote: FakeOrderRemoteDataSource = FakeOrderRemoteDataSource(),
    val chatRemote: FakeChatRemoteDataSource = FakeChatRemoteDataSource(),
    val firebaseAuth: FakeFirebaseAuthGateway = FakeFirebaseAuthGateway(),
    val remoteSync: FakeRemoteSyncGateway = FakeRemoteSyncGateway(),
)

fun createRepositoryTestDeps(): RepositoryTestDeps {
    val dispatcher = kotlinx.coroutines.test.StandardTestDispatcher()
    return RepositoryTestDeps(
        dispatcher = dispatcher,
        scope = CoroutineScope(kotlinx.coroutines.SupervisorJob() + dispatcher),
    )
}

fun createChatRepository(
    database: com.zoti321.c2cmarket.data.local.C2CDatabase,
    authRepository: FakeAuthRepository,
    applicationScope: CoroutineScope,
    ioDispatcher: kotlinx.coroutines.CoroutineDispatcher,
    context: android.content.Context,
    deps: RepositoryTestDeps = createRepositoryTestDeps(),
): ChatRepositoryImpl = ChatRepositoryImpl(
    database = database,
    authRepository = authRepository,
    chatRemote = deps.chatRemote,
    firebaseAuthGateway = deps.firebaseAuth,
    remoteSyncGateway = deps.remoteSync,
    context = context,
    applicationScope = applicationScope,
    ioDispatcher = ioDispatcher,
)

fun createListingRepository(
    database: com.zoti321.c2cmarket.data.local.C2CDatabase,
    authRepository: FakeAuthRepository = FakeAuthRepository(),
    deps: RepositoryTestDeps = createRepositoryTestDeps(),
): ListingRepositoryImpl = ListingRepositoryImpl(
    database = database,
    listingDao = database.listingDao(),
    cartDao = database.cartDao(),
    favoriteDao = database.favoriteDao(),
    orderDao = database.orderDao(),
    authRepository = authRepository,
    listingRemote = deps.listingRemote,
    firebaseAuthGateway = deps.firebaseAuth,
    ioDispatcher = deps.dispatcher,
)

fun createOrderRepository(
    orderDao: OrderDao,
    cartDao: CartDao,
    listingRepository: ListingRepository,
    authRepository: FakeAuthRepository = FakeAuthRepository(),
    scheduler: com.zoti321.c2cmarket.domain.scheduler.OrderNotificationScheduler,
    notificationHelper: com.zoti321.c2cmarket.notification.NotificationHelper,
    deps: RepositoryTestDeps = createRepositoryTestDeps(),
): OrderRepositoryImpl = OrderRepositoryImpl(
    orderDao = orderDao,
    cartDao = cartDao,
    authRepository = authRepository,
    listingRepository = listingRepository,
    notificationHelper = notificationHelper,
    orderNotificationScheduler = scheduler,
    orderRemote = deps.orderRemote,
    firebaseAuthGateway = deps.firebaseAuth,
    applicationScope = deps.scope,
    ioDispatcher = deps.dispatcher,
)

class FakeCartDaoForOrder(
    val items: MutableList<CartItemEntity>,
) : CartDao {
    private val state = MutableStateFlow(items.toList())

    override fun observeAll(userId: String): Flow<List<CartItemEntity>> =
        state.map { list -> list.filter { it.userId == userId } }

    override suspend fun getByProductId(userId: String, id: Int): CartItemEntity? =
        items.firstOrNull { it.userId == userId && it.productId == id }

    override suspend fun insert(item: CartItemEntity) {
        items.removeAll { it.userId == item.userId && it.productId == item.productId }
        items.add(item)
        state.value = items.toList()
    }

    override suspend fun updateQuantity(userId: String, id: Int, qty: Int) = Unit

    override suspend fun updateItem(userId: String, id: Int, title: String, price: Double, imageUrl: String, qty: Int) =
        Unit

    override suspend fun deleteByProductId(userId: String, id: Int) {
        items.removeAll { it.userId == userId && it.productId == id }
        state.value = items.toList()
    }

    override suspend fun deleteByProductIdAllUsers(id: Int) {
        items.removeAll { it.productId == id }
        state.value = items.toList()
    }

    override suspend fun clearForUser(userId: String) {
        items.removeAll { it.userId == userId }
        state.value = items.toList()
    }

    override suspend fun migrateGuestCart(newUserId: String) = Unit
}

class FakeOrderDaoForOrder(
    private val cartDao: FakeCartDaoForOrder,
) : OrderDao {
    var placedLineItems: List<OrderLineItemEntity> = emptyList()
        private set

    override suspend fun insertOrder(order: OrderEntity): Long = 1L

    override suspend fun insertLineItems(items: List<OrderLineItemEntity>) {
        placedLineItems = items
    }

    override suspend fun clearCart(userId: String) {
        cartDao.clearForUser(userId)
    }

    override suspend fun placeOrderWithClearCart(
        order: OrderEntity,
        lineItems: List<OrderLineItemEntity>,
        cartUserId: String,
    ): Long {
        val orderId = insertOrder(order)
        insertLineItems(lineItems.map { it.copy(orderId = orderId) })
        clearCart(cartUserId)
        return orderId
    }

    override fun observeAllOrders(): Flow<List<OrderEntity>> = flowOf(emptyList())

    override fun observeOrdersByUserId(userId: String): Flow<List<OrderEntity>> = flowOf(emptyList())

    override suspend fun migrateGuestOrders(newUserId: String) = Unit

    override fun observeOrder(id: Long): Flow<OrderEntity?> = flowOf(null)

    override fun observeLineItems(orderId: Long): Flow<List<OrderLineItemEntity>> = flowOf(emptyList())

    override fun observeAllLineItems(): Flow<List<OrderLineItemEntity>> = flowOf(emptyList())

    override suspend fun getLocalLineItemProductIds(orderId: Long): List<Int> = emptyList()

    override fun observeOrdersAsSeller(sellerId: String): Flow<List<OrderEntity>> = flowOf(emptyList())

    override suspend fun updateStatus(orderId: Long, status: String) = Unit

    override suspend fun getById(id: Long): OrderEntity? = null

    override suspend fun setBuyerMeetupConfirmed(orderId: Long) = Unit

    override suspend fun setSellerMeetupConfirmed(orderId: Long) = Unit

    override suspend fun findByRemoteId(remoteId: String): OrderEntity? = null

    override suspend fun getPendingOrders(): List<OrderEntity> = emptyList()

    override suspend fun updateRemoteSync(id: Long, remoteId: String, syncState: String) = Unit

    override suspend fun updateRemoteId(id: Long, remoteId: String) = Unit
}

class RecordingOrderNotificationScheduler : OrderNotificationScheduler {
    val scheduledKinds = mutableListOf<OrderNotificationKind>()

    override fun schedule(orderId: Long, orderNumber: String, kind: OrderNotificationKind) {
        scheduledKinds.add(kind)
    }
}
