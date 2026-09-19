package com.zoti321.c2cmarket.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.zoti321.c2cmarket.notification.NotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class OrderShippedNotificationWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val notificationHelper: NotificationHelper,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            if (!notificationHelper.hasNotificationPermission()) {
                return Result.success()
            }
            val orderId = inputData.getLong(KEY_ORDER_ID, -1L)
            val orderNumber = inputData.getString(KEY_ORDER_NUMBER) ?: return Result.failure()
            if (orderId <= 0) return Result.failure()
            notificationHelper.showOrderShipped(orderId, orderNumber)
            Result.success()
        } catch (_: Exception) {
            Result.failure()
        }
    }

    companion object {
        const val KEY_ORDER_ID = "order_id"
        const val KEY_ORDER_NUMBER = "order_number"
    }
}
