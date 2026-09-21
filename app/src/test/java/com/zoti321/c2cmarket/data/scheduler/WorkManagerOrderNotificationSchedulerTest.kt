package com.zoti321.c2cmarket.data.scheduler

import android.util.Log
import androidx.test.core.app.ApplicationProvider
import androidx.work.Configuration
import androidx.work.WorkInfo
import androidx.work.testing.SynchronousExecutor
import androidx.work.testing.WorkManagerTestInitHelper
import com.zoti321.c2cmarket.domain.scheduler.OrderNotificationKind
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
    fun schedule_shipped_enqueuesUniqueWorkWithDelay() {
        assertScheduled(OrderNotificationKind.SHIPPED, "order_notify_shipped_7")
    }

    @Test
    fun schedule_pendingSeller_usesKindSpecificWorkName() {
        assertScheduled(OrderNotificationKind.PENDING_SELLER, "order_notify_pending_seller_7")
    }

    @Test
    fun schedule_confirmedBuyer_usesKindSpecificWorkName() {
        assertScheduled(OrderNotificationKind.CONFIRMED_BUYER, "order_notify_confirmed_buyer_7")
    }

    @Test
    fun schedule_completed_usesKindSpecificWorkName() {
        assertScheduled(OrderNotificationKind.COMPLETED, "order_notify_completed_7")
    }

    private fun assertScheduled(kind: OrderNotificationKind, uniqueWorkName: String) {
        scheduler.schedule(orderId = 7L, orderNumber = "ORD-000007", kind = kind)

        val workManager = androidx.work.WorkManager.getInstance(
            ApplicationProvider.getApplicationContext(),
        )
        val workInfos = workManager.getWorkInfosForUniqueWork(uniqueWorkName).get()
        assertEquals(1, workInfos.size)
        assertEquals(WorkInfo.State.ENQUEUED, workInfos.first().state)
    }
}
