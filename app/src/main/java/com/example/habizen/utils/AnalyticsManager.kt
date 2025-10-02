package com.example.habizen.utils

import android.content.Context
import com.example.habizen.data.DailyStat
import com.example.habizen.data.Habit
import com.example.habizen.data.HydrationData
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale

object AnalyticsManager {

    private val storageDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val displayDateFormat = SimpleDateFormat("EEE", Locale.getDefault())

    fun getHabitSummary(context: Context): HabitSummary {
        val habits = PreferencesManager.getHabits(context)
        if (habits.isEmpty()) {
            return HabitSummary(0, 0f, 0)
        }

        val todayString = storageDateFormat.format(Calendar.getInstance().time)
        val completedToday = habits.count { it.completionDates.contains(todayString) }
        val completionRate = (completedToday.toFloat() / habits.size) * 100f
        val longestStreak = habits.maxOfOrNull { calculateLongestStreak(it) } ?: 0

        return HabitSummary(habits.size, completionRate, longestStreak)
    }

    fun getHabitWeeklyCompletion(context: Context): List<DailyStat> {
        val habits = PreferencesManager.getHabits(context)
        if (habits.isEmpty()) return emptyList()

        val calendar = Calendar.getInstance()
        val stats = mutableListOf<DailyStat>()

        for (i in 6 downTo 0) {
            calendar.timeInMillis = System.currentTimeMillis()
            calendar.add(Calendar.DAY_OF_YEAR, -i)
            val date = calendar.time
            val dateKey = storageDateFormat.format(date)
            val completedCount = habits.count { it.completionDates.contains(dateKey) }
            val percentage = if (habits.isNotEmpty()) {
                (completedCount.toFloat() / habits.size) * 100f
            } else {
                0f
            }
            stats.add(DailyStat(displayDateFormat.format(date), percentage))
        }

        return stats
    }

    fun getMoodSummary(context: Context): MoodSummary {
        val entries = PreferencesManager.getMoodEntries(context)
        if (entries.isEmpty()) {
            return MoodSummary(emptyMap(), 0f)
        }

        val cutoff = System.currentTimeMillis() - LAST_THIRTY_DAYS
        val recentEntries = entries.filter { it.timestamp >= cutoff }
        if (recentEntries.isEmpty()) {
            return MoodSummary(emptyMap(), 0f)
        }

        val distribution = recentEntries.groupingBy { it.emoji }.eachCount()
        val average = recentEntries.map { it.getMoodValue() }.average().toFloat()

        return MoodSummary(distribution, average)
    }

    fun getHydrationSummary(context: Context): HydrationSummary {
        val entries = PreferencesManager.getHydrationEntries(context)
        if (entries.isEmpty()) {
            return HydrationSummary(emptyList(), 0, 0)
        }

        val todayString = storageDateFormat.format(Calendar.getInstance().time)
        val todayTotal = entries.filter { it.date == todayString }.sumOf { it.amount }
        val goal = PreferencesManager.getHydrationGoal(context)

        return HydrationSummary(getHydrationWeeklyTotals(entries), todayTotal, goal)
    }

    private fun getHydrationWeeklyTotals(entries: List<HydrationData>): List<DailyStat> {
        if (entries.isEmpty()) return emptyList()

        val calendar = Calendar.getInstance()
        val stats = mutableListOf<DailyStat>()

        for (i in 6 downTo 0) {
            calendar.timeInMillis = System.currentTimeMillis()
            calendar.add(Calendar.DAY_OF_YEAR, -i)
            val date = calendar.time
            val dateKey = storageDateFormat.format(date)
            val total = entries.filter { it.date == dateKey }.sumOf { it.amount }
            stats.add(DailyStat(displayDateFormat.format(date), total.toFloat()))
        }

        return stats
    }

    private fun calculateLongestStreak(habit: Habit): Int {
        if (habit.completionDates.isEmpty()) return 0

        val formatter = DateTimeFormatter.ISO_LOCAL_DATE
        val dates = habit.completionDates
            .mapNotNull {
                try {
                    LocalDate.parse(it, formatter)
                } catch (_: Exception) {
                    null
                }
            }
            .sorted()

        if (dates.isEmpty()) return 0

        var longest = 1
        var current = 1

        for (i in 1 until dates.size) {
            val previous = dates[i - 1]
            val currentDate = dates[i]
            if (previous.plusDays(1) == currentDate) {
                current += 1
            } else if (previous != currentDate) {
                current = 1
            }
            if (current > longest) {
                longest = current
            }
        }

        return longest
    }

    data class HabitSummary(
        val totalHabits: Int,
        val completionRate: Float,
        val longestStreak: Int
    )

    data class MoodSummary(
        val distribution: Map<String, Int>,
        val averageScore: Float
    )

    data class HydrationSummary(
        val weeklyTotals: List<DailyStat>,
        val todayTotal: Int,
        val dailyGoal: Int
    )

    private const val LAST_THIRTY_DAYS = 1000L * 60L * 60L * 24L * 30L
}
