package com.example.habizen.data

import java.util.*

data class Habit(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val emoji: String,
    val goal: Int,
    val currentProgress: Int = 0,
    val dateCreated: Long = System.currentTimeMillis(),
    val completionDates: MutableList<String> = mutableListOf(),
    val category: String = "General",
    val reminderEnabled: Boolean = false,
    val reminderTime: String = "08:00"
) {
    fun isCompletedToday(): Boolean {
        val today = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            .format(Date())
        return completionDates.contains(today)
    }
    
    fun getCompletionPercentage(): Float {
        return if (goal > 0) (currentProgress.toFloat() / goal) * 100 else 0f
    }
    
    fun markCompleted() {
        val today = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            .format(Date())
        if (!completionDates.contains(today)) {
            completionDates.add(today)
        }
    }
}
