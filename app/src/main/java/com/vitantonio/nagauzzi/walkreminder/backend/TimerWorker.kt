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
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.data.Field
import com.google.android.gms.fitness.request.DataReadRequest
import java.text.SimpleDateFormat
import java.util.*

class TimerWorker(private val context: Context, workerParameters: WorkerParameters) :
    Worker(context, workerParameters) {
    override fun doWork(): Result {
        Log.d(LOG_TAG, "TimerWorker: onStartJob")
        val endTime = Date().time
        val startTime = endTime - 604800000 // A week ago.
        val request = DataReadRequest.Builder()
            .read(DataType.TYPE_STEP_COUNT_CUMULATIVE)
            .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
            .build()
        Fitness.getHistoryClient(context, context.googleAccount!!)
            .readData(request)
            .addOnSuccessListener { response ->
                Log.d(LOG_TAG, "Get data from Google Fit: Success.")
                val dataSet = response.getDataSet(DataType.TYPE_STEP_COUNT_CUMULATIVE)
                val message = dataSet.dataPoints.map { point ->
                    Pair(
                        point.getEndTime(TimeUnit.MILLISECONDS),
                        point.getValue(Field.FIELD_STEPS)
                    )
                }.maxByOrNull { (endTime, _) ->
                    endTime
                }?.let { (endTime, steps) ->
                    val endDate =
                        SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.JAPAN).format(Date(endTime))
                    Log.d(LOG_TAG, "Get data from Google Fit: endDate=$endDate, steps=$steps")
                    "${endDate}に${steps}歩歩いたのが最後です。"
                } ?: "起動してから一度も歩いていません。"
                val manager =
                    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                val channel = NotificationChannel(
                    TIMER_NOTIFICATION_CHANNEL_ID,
                    context.getString(R.string.app_name),
                    NotificationManager.IMPORTANCE_DEFAULT
                )
                channel.lightColor = Color.GREEN
                channel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
                manager.createNotificationChannel(channel)
                val notification =
                    Notification.Builder(applicationContext, TIMER_NOTIFICATION_CHANNEL_ID)
                        .apply {
                            setContentTitle("WalkReminder")
                            setContentText(message)
                            setSmallIcon(R.drawable.ic_launcher_background)
                        }.build()
                manager.notify(TIMER_NOTIFICATION_ID, notification)
            }
            .addOnFailureListener { exception ->
                Log.d(LOG_TAG, "Get data from Google Fit: $exception")
            }
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
    val work = WorkManager.getInstance()
    // TODO: work.getWorkInfoByIdLiveData でMainActivityに伝搬する
    work.enqueueUniquePeriodicWork(
        "TimerWork", ExistingPeriodicWorkPolicy.KEEP, createTimerWork()
    )
}
