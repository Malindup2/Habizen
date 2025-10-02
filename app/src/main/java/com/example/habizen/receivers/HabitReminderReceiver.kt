package com.example.habizen.receivers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.habizen.R
import com.example.habizen.ui.main.MainActivity
import com.example.habizen.utils.HabitReminderScheduler
import com.example.habizen.utils.PreferencesManager

class HabitReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_REMIND_HABIT) return

        val habitId = intent.getStringExtra(EXTRA_HABIT_ID) ?: return
        val habit = PreferencesManager.getHabits(context).firstOrNull { it.id == habitId }
            ?: return
        if (!habit.reminderEnabled) {
            HabitReminderScheduler.cancelHabitReminder(context, habitId)
            return
        }

        ensureNotificationChannel(context)

        val launchIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("fragment", "habits")
        }
        val pendingIntent = androidx.core.app.PendingIntentCompat.getActivity(
            context,
            habitId.hashCode(),
            launchIntent,
            0,
            true
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_home)
            .setColor(ContextCompat.getColor(context, R.color.primary))
            .setContentTitle("${habit.emoji} ${habit.name}")
            .setContentText("It's time to complete this habit")
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        NotificationManagerCompat.from(context).notify(habitId.hashCode(), notification)
        HabitReminderScheduler.scheduleHabitReminder(context, habit)
    }

    private fun ensureNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                .createNotificationChannel(channel)
        }
    }

    companion object {
        const val ACTION_REMIND_HABIT = "com.example.habizen.ACTION_REMIND_HABIT"
        const val EXTRA_HABIT_ID = "extra_habit_id"
        private const val CHANNEL_ID = "habit_reminders_channel"
        private const val CHANNEL_NAME = "Habit Reminders"
    }
}
