package com.vitantonio.nagauzzi.walkreminder.backend

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.util.Log
import androidx.work.*
import com.vitantonio.nagauzzi.walkreminder.LOG_TAG
import com.vitantonio.nagauzzi.walkreminder.R
import com.vitantonio.nagauzzi.walkreminder.TIMER_NOTIFICATION_CHANNEL_ID
import com.vitantonio.nagauzzi.walkreminder.TIMER_NOTIFICATION_ID
import java.util.concurrent.TimeUnit

import android.app.NotificationChannel
import android.graphics.Color


class TimerWorker(private val context: Context, workerParameters: WorkerParameters) :
    Worker(context, workerParameters) {
    override fun doWork(): Result {
        Log.d(LOG_TAG, "TimerWorker: onStartJob")
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            TIMER_NOTIFICATION_CHANNEL_ID,
            context.getString(R.string.app_name),
            NotificationManager.IMPORTANCE_DEFAULT
        )
        channel.lightColor = Color.GREEN
        channel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        manager.createNotificationChannel(channel)
        val notification = Notification.Builder(applicationContext, TIMER_NOTIFICATION_CHANNEL_ID)
            .apply {
                setContentTitle("WalkReminder")
                setContentText("チッスチッス")
                setSmallIcon(R.drawable.ic_launcher_background)
            }.build()
        manager.notify(TIMER_NOTIFICATION_ID, notification)
        return Result.success()
    }
}

private fun createTimerConstraints() =
    Constraints.Builder()
        .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
        .setRequiresBatteryNotLow(true)
        .setRequiresCharging(false)
        .setRequiresStorageNotLow(true)
        .build()

private fun createTimerWork() =
    PeriodicWorkRequestBuilder<TimerWorker>(15, TimeUnit.MINUTES)
        .setConstraints(createTimerConstraints())
        .setBackoffCriteria(
            BackoffPolicy.LINEAR,
            PeriodicWorkRequest.MIN_BACKOFF_MILLIS,
            TimeUnit.MILLISECONDS
        )
        .build()

fun startTimer() {
    WorkManager.getInstance().enqueueUniquePeriodicWork(
        "TimerWork", ExistingPeriodicWorkPolicy.KEEP, createTimerWork()
    )
}
