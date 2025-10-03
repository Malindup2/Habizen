package com.example.habizen.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.example.habizen.data.Habit
import com.example.habizen.receivers.HabitReminderReceiver
import java.util.Calendar

object HabitReminderScheduler {

	fun scheduleHabitReminder(context: Context, habit: Habit) {
		cancelHabitReminder(context, habit.id)

		if (!habit.reminderEnabled) return

		val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
		val triggerTime = calculateTriggerTimeMillis(habit.reminderTime)
		val pendingIntent = createPendingIntent(context, habit)

		// Check if we can schedule exact alarms
		val canScheduleExact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
			alarmManager.canScheduleExactAlarms()
		} else {
			// For older versions, check if we have the permission
			ContextCompat.checkSelfPermission(context, android.Manifest.permission.SCHEDULE_EXACT_ALARM) == PackageManager.PERMISSION_GRANTED
		}

		if (canScheduleExact) {
			// Use exact alarm if permission is granted
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
				alarmManager.setExactAndAllowWhileIdle(
					AlarmManager.RTC_WAKEUP,
					triggerTime,
					pendingIntent
				)
			} else {
				alarmManager.setExact(
					AlarmManager.RTC_WAKEUP,
					triggerTime,
					pendingIntent
				)
			}
		} else {
			// Fall back to inexact alarm if exact permission not granted
			alarmManager.set(
				AlarmManager.RTC_WAKEUP,
				triggerTime,
				pendingIntent
			)
		}
	}

	fun cancelHabitReminder(context: Context, habitId: String) {
		val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
		val pendingIntent = createPendingIntent(context, habitId)
		alarmManager.cancel(pendingIntent)
		pendingIntent.cancel()
	}

	private fun calculateTriggerTimeMillis(time: String): Long {
		val parts = time.split(":")
		val hour = parts.getOrNull(0)?.toIntOrNull() ?: 8
		val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0

		val calendar = Calendar.getInstance().apply {
			set(Calendar.HOUR_OF_DAY, hour)
			set(Calendar.MINUTE, minute)
			set(Calendar.SECOND, 0)
			set(Calendar.MILLISECOND, 0)
			if (timeInMillis <= System.currentTimeMillis()) {
				add(Calendar.DAY_OF_YEAR, 1)
			}
		}
		return calendar.timeInMillis
	}

	private fun createPendingIntent(context: Context, habit: Habit): PendingIntent {
		val intent = Intent(context, HabitReminderReceiver::class.java).apply {
			action = HabitReminderReceiver.ACTION_REMIND_HABIT
			putExtra(HabitReminderReceiver.EXTRA_HABIT_ID, habit.id)
		}

		return PendingIntent.getBroadcast(
			context,
			habit.id.hashCode(),
			intent,
			PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
		)
	}

	private fun createPendingIntent(context: Context, habitId: String): PendingIntent {
		val intent = Intent(context, HabitReminderReceiver::class.java).apply {
			action = HabitReminderReceiver.ACTION_REMIND_HABIT
			putExtra(HabitReminderReceiver.EXTRA_HABIT_ID, habitId)
		}

		return PendingIntent.getBroadcast(
			context,
			habitId.hashCode(),
			intent,
			PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
		)
	}
}
