package com.example.weatherapp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters

class WeatherWorker(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {
    override fun doWork(): Result {
        val prefs = applicationContext.getSharedPreferences("weather_prefs", Context.MODE_PRIVATE)
        val weatherInfo = prefs.getString("weather_summary", applicationContext.getString(R.string.no_weather_txt))

        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "weather_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Hava Durumu", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.ic_main)
            .setContentTitle(applicationContext.getString(R.string.daily_weather))
            .setContentText(weatherInfo)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(3001, notification)

        return Result.success()
    }
}