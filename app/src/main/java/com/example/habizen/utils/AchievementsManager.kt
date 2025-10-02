package com.example.habizen.utils

import android.content.Context
import com.example.habizen.data.Achievement
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object AchievementsManager {

    fun getAchievements(context: Context): List<Achievement> {
        val habits = PreferencesManager.getHabits(context)
        val moods = PreferencesManager.getMoodEntries(context)
        val hydration = PreferencesManager.getHydrationEntries(context)
        val hydrationGoal = PreferencesManager.getHydrationGoal(context)

        val totalHabitCompletions = habits.sumOf { it.completionDates.size }
        val longestStreak = calculateLongestStreak(habits.map { it.completionDates })
        val hydrationGoalDays = calculateHydrationGoalDays(hydration, hydrationGoal)

        val achievements = mutableListOf<Achievement>()

        achievements += Achievement(
            id = "first_habit",
            title = "Habit Starter",
            description = "Create your first habit",
            emoji = "🌱",
            isUnlocked = habits.isNotEmpty(),
            progress = habits.size.coerceAtMost(1),
            target = 1
        )

        achievements += Achievement(
            id = "habit_master",
            title = "Consistency Builder",
            description = "Complete habits 25 times",
            emoji = "🏆",
            isUnlocked = totalHabitCompletions >= 25,
            progress = totalHabitCompletions.coerceAtMost(25),
            target = 25
        )

        achievements += Achievement(
            id = "streak_week",
            title = "7-Day Streak",
            description = "Maintain any habit for 7 days straight",
            emoji = "🔥",
            isUnlocked = longestStreak >= 7,
            progress = longestStreak.coerceAtMost(7),
            target = 7
        )

        achievements += Achievement(
            id = "hydration_champ",
            title = "Hydration Champ",
            description = "Log 20 water entries",
            emoji = "💧",
            isUnlocked = hydration.size >= 20,
            progress = hydration.size.coerceAtMost(20),
            target = 20
        )

        achievements += Achievement(
            id = "hydration_steady",
            title = "Hydrated Week",
            description = "Hit your water goal on 5 days this week",
            emoji = "🚰",
            isUnlocked = hydrationGoalDays >= 5,
            progress = hydrationGoalDays.coerceAtMost(5),
            target = 5
        )

        achievements += Achievement(
            id = "mood_reflector",
            title = "Mood Reflector",
            description = "Log 10 moods",
            emoji = "😊",
            isUnlocked = moods.size >= 10,
            progress = moods.size.coerceAtMost(10),
            target = 10
        )

        return achievements
    }

    private fun calculateLongestStreak(completionLists: List<List<String>>): Int {
        val formatter = DateTimeFormatter.ISO_LOCAL_DATE
        var longest = 0

        completionLists.forEach { dates ->
            val parsed = dates.mapNotNull {
                try {
                    LocalDate.parse(it, formatter)
                } catch (_: Exception) {
                    null
                }
            }.sorted()

            var current = 1
            for (i in 1 until parsed.size) {
                val prev = parsed[i - 1]
                val currentDate = parsed[i]
                current = when {
                    prev.plusDays(1) == currentDate -> current + 1
                    prev == currentDate -> current
                    else -> 1
                }
                longest = maxOf(longest, current)
            }
            if (parsed.size == 1) {
                longest = maxOf(longest, 1)
            }
        }

        return longest
    }

    private fun calculateHydrationGoalDays(entries: List<com.example.habizen.data.HydrationData>, goal: Int): Int {
        if (entries.isEmpty()) return 0
        val grouped = entries.groupBy { it.date }
        val sevenDaysAgo = LocalDate.now().minusDays(6)
        return grouped.entries.count { (date, logs) ->
            try {
                val entryDate = LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE)
                entryDate >= sevenDaysAgo && logs.sumOf { it.amount } >= goal
            } catch (e: Exception) {
                false
            }
        }
    }
}
