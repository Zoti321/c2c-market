package com.zoti321.c2cmarket.di

import com.zoti321.c2cmarket.data.repository.AddressRepositoryImpl
import com.zoti321.c2cmarket.data.repository.BrowseHistoryRepositoryImpl
import com.zoti321.c2cmarket.data.repository.CartRepositoryImpl
import com.zoti321.c2cmarket.data.repository.ChatRepositoryImpl
import com.zoti321.c2cmarket.data.repository.FakeStoreProductRepository
import com.zoti321.c2cmarket.data.repository.FavoriteRepositoryImpl
import com.zoti321.c2cmarket.data.repository.ListingRepositoryImpl
import com.zoti321.c2cmarket.data.repository.OrderRepositoryImpl
import com.zoti321.c2cmarket.domain.repository.AddressRepository
import com.zoti321.c2cmarket.domain.repository.BrowseHistoryRepository
import com.zoti321.c2cmarket.domain.repository.CartRepository
import com.zoti321.c2cmarket.domain.repository.ChatRepository
import com.zoti321.c2cmarket.domain.repository.FavoriteRepository
import com.zoti321.c2cmarket.domain.repository.ListingRepository
import com.zoti321.c2cmarket.domain.repository.OrderRepository
import com.zoti321.c2cmarket.domain.repository.ProductRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindProductRepository(impl: FakeStoreProductRepository): ProductRepository

    @Binds
    @Singleton
    abstract fun bindCartRepository(impl: CartRepositoryImpl): CartRepository

    @Binds
    @Singleton
    abstract fun bindFavoriteRepository(impl: FavoriteRepositoryImpl): FavoriteRepository

    @Binds
    @Singleton
    abstract fun bindOrderRepository(impl: OrderRepositoryImpl): OrderRepository

    @Binds
    @Singleton
    abstract fun bindBrowseHistoryRepository(impl: BrowseHistoryRepositoryImpl): BrowseHistoryRepository

    @Binds
    @Singleton
    abstract fun bindListingRepository(impl: ListingRepositoryImpl): ListingRepository

    @Binds
    @Singleton
    abstract fun bindAddressRepository(impl: AddressRepositoryImpl): AddressRepository

    @Binds
    @Singleton
    abstract fun bindChatRepository(impl: ChatRepositoryImpl): ChatRepository
}
