package com.zoti321.c2cmarket.di

import com.zoti321.c2cmarket.data.auth.GoogleCredentialDataSource
import com.zoti321.c2cmarket.data.auth.GoogleCredentialDataSourceImpl
import com.zoti321.c2cmarket.data.migration.GuestDataMigration
import com.zoti321.c2cmarket.data.migration.GuestDataMigrator
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthModule {

    @Binds
    @Singleton
    abstract fun bindGoogleCredentialDataSource(
        impl: GoogleCredentialDataSourceImpl,
    ): GoogleCredentialDataSource

    @Binds
    @Singleton
    abstract fun bindGuestDataMigration(impl: GuestDataMigrator): GuestDataMigration
}
