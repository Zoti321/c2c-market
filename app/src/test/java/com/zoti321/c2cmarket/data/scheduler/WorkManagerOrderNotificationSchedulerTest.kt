package com.zoti321.c2cmarket.data.scheduler

import android.util.Log
import androidx.test.core.app.ApplicationProvider
import androidx.work.Configuration
import androidx.work.WorkInfo
import androidx.work.testing.SynchronousExecutor
import androidx.work.testing.WorkManagerTestInitHelper
import com.zoti321.c2cmarket.notification.NotificationHelper
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class WorkManagerOrderNotificationSchedulerTest {

    private lateinit var scheduler: WorkManagerOrderNotificationScheduler

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val config = Configuration.Builder()
            .setMinimumLoggingLevel(Log.ERROR)
            .setExecutor(SynchronousExecutor())
            .build()
        WorkManagerTestInitHelper.initializeTestWorkManager(context, config)
        scheduler = WorkManagerOrderNotificationScheduler(
            workManager = androidx.work.WorkManager.getInstance(context),
            notificationHelper = NotificationHelper(context),
        )
    }

    @Test
    fun schedule_enqueuesUniqueWorkWithDelay() {
        scheduler.schedule(orderId = 7L, orderNumber = "ORD-000007")

        val workManager = androidx.work.WorkManager.getInstance(
            ApplicationProvider.getApplicationContext(),
        )
        val workInfos = workManager
            .getWorkInfosForUniqueWork("order_ship_notify_7")
            .get()
        assertEquals(1, workInfos.size)
        assertEquals(WorkInfo.State.ENQUEUED, workInfos.first().state)
    }
}
