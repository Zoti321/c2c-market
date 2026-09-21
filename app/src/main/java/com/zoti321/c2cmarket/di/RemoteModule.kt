package com.zoti321.c2cmarket.di

import com.zoti321.c2cmarket.data.firebase.FirebaseListingRemoteDataSource
import com.zoti321.c2cmarket.data.firebase.FirestoreChatRemoteDataSource
import com.zoti321.c2cmarket.data.firebase.FirestoreOrderRemoteDataSource
import com.zoti321.c2cmarket.domain.datasource.ChatRemoteDataSource
import com.zoti321.c2cmarket.domain.datasource.ListingRemoteDataSource
import com.zoti321.c2cmarket.domain.datasource.OrderRemoteDataSource
import com.zoti321.c2cmarket.data.sync.RemoteSyncCoordinator
import com.zoti321.c2cmarket.data.sync.RemoteSyncGateway
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RemoteModule {

    @Binds
    @Singleton
    abstract fun bindRemoteSyncGateway(impl: RemoteSyncCoordinator): RemoteSyncGateway

    @Binds
    @Singleton
    abstract fun bindChatRemoteDataSource(impl: FirestoreChatRemoteDataSource): ChatRemoteDataSource

    @Binds
    @Singleton
    abstract fun bindListingRemoteDataSource(impl: FirebaseListingRemoteDataSource): ListingRemoteDataSource

    @Binds
    @Singleton
    abstract fun bindOrderRemoteDataSource(impl: FirestoreOrderRemoteDataSource): OrderRemoteDataSource
}
