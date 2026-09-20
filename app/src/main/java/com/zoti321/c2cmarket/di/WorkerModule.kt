package com.zoti321.c2cmarket.di

import android.content.Context
import androidx.work.WorkManager
import com.zoti321.c2cmarket.data.scheduler.WorkManagerOrderNotificationScheduler
import com.zoti321.c2cmarket.domain.scheduler.OrderNotificationScheduler
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class WorkerModule {

    @Binds
    @Singleton
    abstract fun bindOrderNotificationScheduler(
        impl: WorkManagerOrderNotificationScheduler,
    ): OrderNotificationScheduler

    companion object {
        @Provides
        @Singleton
        fun provideWorkManager(@ApplicationContext context: Context): WorkManager =
            WorkManager.getInstance(context)
    }
}
