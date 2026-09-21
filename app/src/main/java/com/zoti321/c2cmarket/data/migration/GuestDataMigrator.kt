package com.zoti321.c2cmarket.data.migration

import androidx.room.withTransaction
import com.zoti321.c2cmarket.data.local.C2CDatabase
import com.zoti321.c2cmarket.data.local.dao.CartDao
import com.zoti321.c2cmarket.data.local.dao.ConversationDao
import com.zoti321.c2cmarket.data.local.dao.FavoriteDao
import com.zoti321.c2cmarket.data.local.dao.ListingDao
import com.zoti321.c2cmarket.data.local.dao.OrderDao
import com.zoti321.c2cmarket.domain.model.UserIds
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GuestDataMigrator @Inject constructor(
    private val database: C2CDatabase,
    private val orderDao: OrderDao,
    private val listingDao: ListingDao,
    private val conversationDao: ConversationDao,
    private val cartDao: CartDao,
    private val favoriteDao: FavoriteDao,
) : GuestDataMigration {
    override suspend fun migrateGuestDataTo(newUserId: String) {
        database.withTransaction {
            orderDao.migrateGuestOrders(newUserId)
            listingDao.migrateGuestListings(newUserId)
            conversationDao.migrateGuestConversations(newUserId)
            cartDao.migrateGuestCart(newUserId)
            favoriteDao.migrateGuestFavorites(newUserId)
        }
    }

    companion object {
        const val GUEST_ID = UserIds.GUEST
    }
}
