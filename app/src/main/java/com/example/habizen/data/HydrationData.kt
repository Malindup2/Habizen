package com.example.habizen.data

import java.util.*

data class HydrationData(
    val id: String = UUID.randomUUID().toString(),
    val amount: Int, // in ml
    val time: String,
    val date: String
)

data class HydrationLog(
    val id: String = UUID.randomUUID().toString(),
    val amount: Int, // in ml
    val timestamp: Long = System.currentTimeMillis(),
    val dateString: String = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        .format(Date())
)

data class HydrationSettings(
    val dailyGoal: Int = 2000, // ml
    val reminderInterval: Int = 60, // minutes
    val startTime: String = "08:00",
    val endTime: String = "22:00",
    val notificationsEnabled: Boolean = true
)
