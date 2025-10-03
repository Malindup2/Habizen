package com.example.habizen.utils

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.habizen.MainActivity
import com.example.habizen.R

object GoalCompletionNotificationManager {

    private const val CHANNEL_ID = "goal_completion_channel"
    private const val CHANNEL_NAME = "Goal Completion"
    private const val CHANNEL_DESCRIPTION = "Notifications for completed goals and achievements"

    private const val HABIT_COMPLETION_NOTIFICATION_ID = 2001
    private const val HYDRATION_GOAL_NOTIFICATION_ID = 2002
    private const val ACHIEVEMENT_NOTIFICATION_ID = 2003

    fun initialize(context: Context) {
        createNotificationChannel(context)
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESCRIPTION
                enableVibration(true)
                enableLights(true)
            }

            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun sendHabitCompletionNotification(context: Context, habitName: String) {
        Log.d("GoalCompletionNotification", "Attempting to send habit completion notification for: $habitName")

        // Ensure notification channel is created
        createNotificationChannel(context)

        if (!hasNotificationPermission(context)) {
            Log.w("GoalCompletionNotification", "Notification permission not granted")
            return
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "habits")
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            HABIT_COMPLETION_NOTIFICATION_ID,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("🎉 Habit Completed!")
            .setContentText("Congratulations! You've completed your \"$habitName\" habit for today.")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("Congratulations! You've completed your \"$habitName\" habit for today. Keep up the amazing work!"))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(HABIT_COMPLETION_NOTIFICATION_ID, notification)
            Log.d("GoalCompletionNotification", "Habit completion notification sent successfully")
        } catch (e: SecurityException) {
            Log.e("GoalCompletionNotification", "Failed to send notification: ${e.message}")
        } catch (e: Exception) {
            Log.e("GoalCompletionNotification", "Unexpected error sending notification: ${e.message}")
        }
    }

    fun sendHydrationGoalNotification(context: Context, goalAmount: Int) {
        Log.d("GoalCompletionNotification", "Attempting to send hydration goal notification for: ${goalAmount}ml")

        // Ensure notification channel is created
        createNotificationChannel(context)

        if (!hasNotificationPermission(context)) {
            Log.w("GoalCompletionNotification", "Notification permission not granted")
            return
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "hydration")
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            HYDRATION_GOAL_NOTIFICATION_ID,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("💧 Hydration Goal Achieved!")
            .setContentText("Amazing! You've reached your daily hydration goal of ${goalAmount}ml.")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("Amazing! You've reached your daily hydration goal of ${goalAmount}ml. Your body will thank you!"))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(HYDRATION_GOAL_NOTIFICATION_ID, notification)
            Log.d("GoalCompletionNotification", "Hydration goal notification sent successfully")
        } catch (e: SecurityException) {
            Log.e("GoalCompletionNotification", "Failed to send notification: ${e.message}")
        } catch (e: Exception) {
            Log.e("GoalCompletionNotification", "Unexpected error sending notification: ${e.message}")
        }
    }

    fun sendAchievementNotification(context: Context, achievementTitle: String, achievementDescription: String) {
        Log.d("GoalCompletionNotification", "Attempting to send achievement notification for: $achievementTitle")

        // Ensure notification channel is created
        createNotificationChannel(context)

        if (!hasNotificationPermission(context)) {
            Log.w("GoalCompletionNotification", "Notification permission not granted")
            return
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "profile")
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            ACHIEVEMENT_NOTIFICATION_ID,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("🏆 Achievement Unlocked!")
            .setContentText(achievementTitle)
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("$achievementTitle\n\n$achievementDescription"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(ACHIEVEMENT_NOTIFICATION_ID, notification)
            Log.d("GoalCompletionNotification", "Achievement notification sent successfully")
        } catch (e: SecurityException) {
            Log.e("GoalCompletionNotification", "Failed to send notification: ${e.message}")
        } catch (e: Exception) {
            Log.e("GoalCompletionNotification", "Unexpected error sending notification: ${e.message}")
        }
    }

    fun shouldRequestNotificationPermission(context: Context): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
               ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
    }

    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ requires runtime permission
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        } else {
            // Pre-Android 13, notifications are allowed by default
            true
        }
    }
}