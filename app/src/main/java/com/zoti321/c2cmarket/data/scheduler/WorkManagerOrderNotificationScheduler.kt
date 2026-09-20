package com.zoti321.c2cmarket.data.scheduler

import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.zoti321.c2cmarket.domain.scheduler.OrderNotificationScheduler
import com.zoti321.c2cmarket.notification.NotificationHelper
import com.zoti321.c2cmarket.worker.OrderShippedNotificationWorker
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkManagerOrderNotificationScheduler @Inject constructor(
    private val workManager: WorkManager,
    private val notificationHelper: NotificationHelper,
) : OrderNotificationScheduler {

    override fun schedule(orderId: Long, orderNumber: String) {
        if (!notificationHelper.hasNotificationPermission()) return
        val input = Data.Builder()
            .putLong(OrderShippedNotificationWorker.KEY_ORDER_ID, orderId)
            .putString(OrderShippedNotificationWorker.KEY_ORDER_NUMBER, orderNumber)
            .build()
        val request = OneTimeWorkRequestBuilder<OrderShippedNotificationWorker>()
            .setInitialDelay(ORDER_NOTIFICATION_DELAY_SECONDS, TimeUnit.SECONDS)
            .setInputData(input)
            .build()
        workManager.enqueueUniqueWork(
            "order_ship_notify_$orderId",
            androidx.work.ExistingWorkPolicy.REPLACE,
            request,
        )
    }

    companion object {
        const val ORDER_NOTIFICATION_DELAY_SECONDS = 15L
    }
}
