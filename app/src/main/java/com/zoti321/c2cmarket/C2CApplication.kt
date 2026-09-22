package com.zoti321.c2cmarket

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.zoti321.c2cmarket.data.sync.RemoteSyncGateway
import com.zoti321.c2cmarket.notification.AppForegroundTracker
import com.zoti321.c2cmarket.notification.NotificationHelper
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class C2CApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var remoteSyncGateway: RemoteSyncGateway

    @Inject
    lateinit var notificationHelper: NotificationHelper

    @Inject
    lateinit var foregroundTracker: AppForegroundTracker

    override fun onCreate() {
        super.onCreate()
        notificationHelper.ensureChannels()
        foregroundTracker.register()
        remoteSyncGateway.start()
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
