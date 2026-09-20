package com.zoti321.c2cmarket.notification

import android.annotation.SuppressLint
import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.zoti321.c2cmarket.R
import com.zoti321.c2cmarket.navigation.DeepLinkParser
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun hasNotificationPermission(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun ensureChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java)
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.notification_channel_orders),
            NotificationManager.IMPORTANCE_DEFAULT,
        )
        manager.createNotificationChannel(channel)
    }

    @SuppressLint("MissingPermission")
    fun showOrderShipped(orderId: Long, orderNumber: String) {
        if (!hasNotificationPermission()) return
        ensureChannel()
        val intent = Intent(Intent.ACTION_VIEW, DeepLinkParser.orderUri(orderId)).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            orderId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.notification_order_shipped_title))
            .setContentText(
                context.getString(R.string.notification_order_shipped_body, orderNumber),
            )
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(orderId.toInt(), notification)
    }

    @SuppressLint("MissingPermission")
    fun showOrderPendingSeller(orderId: Long, orderNumber: String) {
        showOrderNotification(
            orderId = orderId,
            notificationId = (orderId * 10 + 1).toInt(),
            title = context.getString(R.string.notification_order_pending_seller_title),
            body = context.getString(R.string.notification_order_pending_seller_body, orderNumber),
        )
    }

    @SuppressLint("MissingPermission")
    fun showOrderConfirmedBuyer(orderId: Long, orderNumber: String) {
        showOrderNotification(
            orderId = orderId,
            notificationId = (orderId * 10 + 2).toInt(),
            title = context.getString(R.string.notification_order_confirmed_buyer_title),
            body = context.getString(R.string.notification_order_confirmed_buyer_body, orderNumber),
        )
    }

    @SuppressLint("MissingPermission")
    fun showOrderCompleted(orderId: Long, orderNumber: String) {
        showOrderNotification(
            orderId = orderId,
            notificationId = (orderId * 10 + 3).toInt(),
            title = context.getString(R.string.notification_order_completed_title),
            body = context.getString(R.string.notification_order_completed_body, orderNumber),
        )
    }

    @SuppressLint("MissingPermission")
    private fun showOrderNotification(
        orderId: Long,
        notificationId: Int,
        title: String,
        body: String,
    ) {
        if (!hasNotificationPermission()) return
        ensureChannel()
        val intent = Intent(Intent.ACTION_VIEW, DeepLinkParser.orderUri(orderId)).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(notificationId, notification)
    }

    companion object {
        const val CHANNEL_ID = "order_updates"
    }
}
