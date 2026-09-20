package com.zoti321.c2cmarket.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.zoti321.c2cmarket.domain.scheduler.OrderNotificationKind
import com.zoti321.c2cmarket.notification.NotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class OrderShippedNotificationWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val notificationHelper: NotificationHelper,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = try {
        if (!notificationHelper.hasNotificationPermission()) {
            Result.success()
        } else {
            deliverNotification()
        }
    } catch (_: Exception) {
        Result.failure()
    }

    private suspend fun deliverNotification(): Result {
        val orderId = inputData.getLong(KEY_ORDER_ID, -1L)
        val orderNumber = inputData.getString(KEY_ORDER_NUMBER)
        if (orderNumber.isNullOrBlank() || orderId <= 0) return Result.failure()
        val kind = inputData.getString(KEY_NOTIFICATION_KIND)?.let {
            runCatching { OrderNotificationKind.valueOf(it) }.getOrNull()
        } ?: OrderNotificationKind.SHIPPED
        when (kind) {
            OrderNotificationKind.SHIPPED ->
                notificationHelper.showOrderShipped(orderId, orderNumber)
            OrderNotificationKind.PENDING_SELLER ->
                notificationHelper.showOrderPendingSeller(orderId, orderNumber)
            OrderNotificationKind.CONFIRMED_BUYER ->
                notificationHelper.showOrderConfirmedBuyer(orderId, orderNumber)
            OrderNotificationKind.COMPLETED ->
                notificationHelper.showOrderCompleted(orderId, orderNumber)
        }
        return Result.success()
    }

    companion object {
        const val KEY_ORDER_ID = "order_id"
        const val KEY_ORDER_NUMBER = "order_number"
        const val KEY_NOTIFICATION_KIND = "notification_kind"
    }
}
