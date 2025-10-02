package com.example.habizen.workers

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.habizen.R
import com.example.habizen.ui.main.MainActivity
import com.example.habizen.utils.PreferencesManager
import java.text.SimpleDateFormat
import java.util.*

class HydrationReminderWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    companion object {
        private const val CHANNEL_ID = "hydration_reminders"
        private const val NOTIFICATION_ID = 1001
        private const val WORK_NAME = "wellnest_hydration_reminders"

        fun schedule(context: Context) {
            val settings = PreferencesManager.getHydrationSettings(context)
            val workManager = WorkManager.getInstance(context)
            if (!settings.notificationsEnabled) {
                workManager.cancelUniqueWork(WORK_NAME)
                return
            }

            val request = PeriodicWorkRequestBuilder<HydrationReminderWorker>(
                settings.reminderInterval.toLong(),
                java.util.concurrent.TimeUnit.MINUTES
            )
                .setInitialDelay(settings.reminderInterval.toLong(), java.util.concurrent.TimeUnit.MINUTES)
                .build()

            workManager.enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
        }
    }

    override fun doWork(): Result {
        return try {
            // Check current intake for today
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val todayEntries = PreferencesManager.getHydrationEntries(applicationContext).filter { it.date == today }
            val currentIntake = todayEntries.sumOf { it.amount }
            val dailyGoal = PreferencesManager.getHydrationGoal(applicationContext)
            
            // Only send notification if user hasn't reached goal and it's daytime
            val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            if (currentIntake < dailyGoal && currentHour in 8..20) {
                createNotificationChannel()
                sendHydrationReminder(currentIntake, dailyGoal)
            }
            
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Hydration Reminders"
            val descriptionText = "Reminders to drink water"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            
            val notificationManager: NotificationManager =
                applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun sendHydrationReminder(currentIntake: Int, dailyGoal: Int) {
        // Create intent to open hydration fragment
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("fragment", "hydration")
        }
        
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val remaining = dailyGoal - currentIntake
        val message = if (remaining > 0) {
            "💧 Time to hydrate! You need ${remaining}ml more to reach your daily goal."
        } else {
            "🎉 Great job! You've reached your hydration goal today!"
        }

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_water_drop)
            .setContentTitle("Stay Hydrated!")
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .addAction(
                R.drawable.ic_water_glass,
                "Quick Add 250ml",
                createQuickAddPendingIntent(250)
            )
            .addAction(
                R.drawable.ic_water_bottle,
                "Quick Add 500ml",
                createQuickAddPendingIntent(500)
            )
            .build()

        if (ActivityCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            NotificationManagerCompat.from(applicationContext)
                .notify(NOTIFICATION_ID, notification)
        }
    }

    private fun createQuickAddPendingIntent(amount: Int): PendingIntent {
        val intent = Intent(applicationContext, HydrationQuickAddReceiver::class.java).apply {
            putExtra("amount", amount)
        }
        return PendingIntent.getBroadcast(
            applicationContext,
            amount, // Use amount as request code to make it unique
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
