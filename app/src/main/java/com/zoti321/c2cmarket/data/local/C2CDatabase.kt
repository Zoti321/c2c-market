package com.zoti321.c2cmarket.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.zoti321.c2cmarket.data.local.dao.AddressDao
import com.zoti321.c2cmarket.data.local.dao.BrowseHistoryDao
import com.zoti321.c2cmarket.data.local.dao.CartDao
import com.zoti321.c2cmarket.data.local.dao.ConversationDao
import com.zoti321.c2cmarket.data.local.dao.FavoriteDao
import com.zoti321.c2cmarket.data.local.dao.ListingDao
import com.zoti321.c2cmarket.data.local.dao.MessageDao
import com.zoti321.c2cmarket.data.local.dao.OrderDao
import com.zoti321.c2cmarket.data.local.entity.AddressEntity
import com.zoti321.c2cmarket.data.local.entity.BrowseHistoryEntity
import com.zoti321.c2cmarket.data.local.entity.CartItemEntity
import com.zoti321.c2cmarket.data.local.entity.ConversationEntity
import com.zoti321.c2cmarket.data.local.entity.FavoriteEntity
import com.zoti321.c2cmarket.data.local.entity.ListingEntity
import com.zoti321.c2cmarket.data.local.entity.MessageEntity
import com.zoti321.c2cmarket.data.local.entity.OrderEntity
import com.zoti321.c2cmarket.data.local.entity.OrderLineItemEntity

@Database(
    entities = [
        CartItemEntity::class,
        FavoriteEntity::class,
        OrderEntity::class,
        OrderLineItemEntity::class,
        BrowseHistoryEntity::class,
        ListingEntity::class,
        AddressEntity::class,
        ConversationEntity::class,
        MessageEntity::class,
    ],
    version = 6,
    exportSchema = true,
)
abstract class C2CDatabase : RoomDatabase() {
    abstract fun cartDao(): CartDao

    abstract fun favoriteDao(): FavoriteDao

    abstract fun orderDao(): OrderDao

    abstract fun browseHistoryDao(): BrowseHistoryDao

    abstract fun listingDao(): ListingDao

    abstract fun addressDao(): AddressDao

    abstract fun conversationDao(): ConversationDao

    abstract fun messageDao(): MessageDao
}
