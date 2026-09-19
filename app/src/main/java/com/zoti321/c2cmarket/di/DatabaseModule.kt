package com.zoti321.c2cmarket.di

import android.content.Context
import androidx.room.Room
import com.zoti321.c2cmarket.data.local.C2CDatabase
import com.zoti321.c2cmarket.data.local.MIGRATION_1_2
import com.zoti321.c2cmarket.data.local.dao.CartDao
import com.zoti321.c2cmarket.data.local.dao.FavoriteDao
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
            .addMigrations(MIGRATION_1_2)
            .build()

    @Provides
    fun provideCartDao(database: C2CDatabase): CartDao = database.cartDao()

    @Provides
    fun provideFavoriteDao(database: C2CDatabase): FavoriteDao = database.favoriteDao()

    @Provides
    fun provideOrderDao(database: C2CDatabase): OrderDao = database.orderDao()
}
