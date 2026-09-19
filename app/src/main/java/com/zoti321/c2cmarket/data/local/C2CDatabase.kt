package com.zoti321.c2cmarket.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.zoti321.c2cmarket.data.local.dao.CartDao
import com.zoti321.c2cmarket.data.local.dao.FavoriteDao
import com.zoti321.c2cmarket.data.local.dao.OrderDao
import com.zoti321.c2cmarket.data.local.entity.CartItemEntity
import com.zoti321.c2cmarket.data.local.entity.FavoriteEntity
import com.zoti321.c2cmarket.data.local.entity.OrderEntity
import com.zoti321.c2cmarket.data.local.entity.OrderLineItemEntity

@Database(
    entities = [
        CartItemEntity::class,
        FavoriteEntity::class,
        OrderEntity::class,
        OrderLineItemEntity::class,
    ],
    version = 2,
    exportSchema = true,
)
abstract class C2CDatabase : RoomDatabase() {
    abstract fun cartDao(): CartDao

    abstract fun favoriteDao(): FavoriteDao

    abstract fun orderDao(): OrderDao
}
