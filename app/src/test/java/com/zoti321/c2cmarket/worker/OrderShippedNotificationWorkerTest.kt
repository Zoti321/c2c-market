package com.zoti321.c2cmarket.worker

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.work.Data
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import androidx.work.testing.TestListenableWorkerBuilder
import com.zoti321.c2cmarket.notification.NotificationHelper
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
class OrderShippedNotificationWorkerTest {

    @Test
    @Config(sdk = [28])
    fun doWork_withPermission_returnsSuccess() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val worker = buildWorker(
            context = context,
            notificationHelper = NotificationHelper(context),
        )

        val result = worker.doWork()

        assertTrue(result is ListenableWorker.Result.Success)
    }

    @Test
    @Config(sdk = [33])
    fun doWork_withoutPermission_skipsAndReturnsSuccess() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val worker = buildWorker(
            context = context,
            notificationHelper = NotificationHelper(context),
        )

        val result = worker.doWork()

        assertTrue(result is ListenableWorker.Result.Success)
    }

    private fun buildWorker(
        context: Context,
        notificationHelper: NotificationHelper,
    ): OrderShippedNotificationWorker {
        val input = Data.Builder()
            .putLong(OrderShippedNotificationWorker.KEY_ORDER_ID, 1L)
            .putString(OrderShippedNotificationWorker.KEY_ORDER_NUMBER, "ORD-001")
            .build()
        return TestListenableWorkerBuilder<OrderShippedNotificationWorker>(context)
            .setWorkerFactory(
                object : WorkerFactory() {
                    override fun createWorker(
                        appContext: Context,
                        workerClassName: String,
                        workerParameters: WorkerParameters,
                    ): ListenableWorker? {
                        if (workerClassName == OrderShippedNotificationWorker::class.java.name) {
                            return OrderShippedNotificationWorker(
                                appContext,
                                workerParameters,
                                notificationHelper,
                            )
                        }
                        return null
                    }
                },
            )
            .setInputData(input)
            .build()
    }
}
