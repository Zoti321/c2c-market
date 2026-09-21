package com.zoti321.c2cmarket.di

import android.content.Context
import androidx.room.Room
import com.zoti321.c2cmarket.data.local.C2CDatabase
import com.zoti321.c2cmarket.data.local.MIGRATION_1_2
import com.zoti321.c2cmarket.data.local.MIGRATION_2_3
import com.zoti321.c2cmarket.data.local.MIGRATION_3_4
import com.zoti321.c2cmarket.data.local.MIGRATION_4_5
import com.zoti321.c2cmarket.data.local.MIGRATION_5_6
import com.zoti321.c2cmarket.data.local.MIGRATION_6_7
import com.zoti321.c2cmarket.data.local.MIGRATION_7_8
import com.zoti321.c2cmarket.data.local.MIGRATION_8_9
import com.zoti321.c2cmarket.data.local.MIGRATION_9_10
import com.zoti321.c2cmarket.data.local.dao.AddressDao
import com.zoti321.c2cmarket.data.local.dao.BrowseHistoryDao
import com.zoti321.c2cmarket.data.local.dao.CartDao
import com.zoti321.c2cmarket.data.local.dao.ConversationDao
import com.zoti321.c2cmarket.data.local.dao.FavoriteDao
import com.zoti321.c2cmarket.data.local.dao.ListingDao
import com.zoti321.c2cmarket.data.local.dao.MessageDao
import com.zoti321.c2cmarket.data.local.dao.OrderDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): C2CDatabase =
        Room.databaseBuilder(
            context,
            C2CDatabase::class.java,
            "c2c_market.db",
        )
            .addMigrations(
                MIGRATION_1_2,
                MIGRATION_2_3,
                MIGRATION_3_4,
                MIGRATION_4_5,
                MIGRATION_5_6,
                MIGRATION_6_7,
                MIGRATION_7_8,
                MIGRATION_8_9,
                MIGRATION_9_10,
            )
            .build()

    @Provides
    fun provideCartDao(database: C2CDatabase): CartDao = database.cartDao()

    @Provides
    fun provideFavoriteDao(database: C2CDatabase): FavoriteDao = database.favoriteDao()

    @Provides
    fun provideOrderDao(database: C2CDatabase): OrderDao = database.orderDao()

    @Provides
    fun provideBrowseHistoryDao(database: C2CDatabase): BrowseHistoryDao =
        database.browseHistoryDao()

    @Provides
    fun provideListingDao(database: C2CDatabase): ListingDao = database.listingDao()

    @Provides
    fun provideAddressDao(database: C2CDatabase): AddressDao = database.addressDao()

    @Provides
    fun provideConversationDao(database: C2CDatabase): ConversationDao = database.conversationDao()

    @Provides
    fun provideMessageDao(database: C2CDatabase): MessageDao = database.messageDao()
}
