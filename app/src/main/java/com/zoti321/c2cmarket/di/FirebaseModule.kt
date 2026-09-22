package com.zoti321.c2cmarket.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.zoti321.c2cmarket.data.firebase.FirebaseAuthGateway
import com.zoti321.c2cmarket.data.firebase.FirebaseAuthGatewayImpl
import com.zoti321.c2cmarket.data.firebase.UserMappingGateway
import com.zoti321.c2cmarket.data.firebase.UserMappingRepository
import com.zoti321.c2cmarket.notification.AppForegroundState
import com.zoti321.c2cmarket.notification.AppForegroundTracker
import com.zoti321.c2cmarket.notification.NotificationHelper
import com.zoti321.c2cmarket.notification.PushNotificationPresenter
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirebaseProvidesModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage = FirebaseStorage.getInstance()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class FirebaseBindsModule {

    @Binds
    @Singleton
    abstract fun bindFirebaseAuthGateway(impl: FirebaseAuthGatewayImpl): FirebaseAuthGateway

    @Binds
    @Singleton
    abstract fun bindUserMappingGateway(impl: UserMappingRepository): UserMappingGateway

    @Binds
    @Singleton
    abstract fun bindAppForegroundState(impl: AppForegroundTracker): AppForegroundState

    @Binds
    @Singleton
    abstract fun bindPushNotificationPresenter(impl: NotificationHelper): PushNotificationPresenter
}
