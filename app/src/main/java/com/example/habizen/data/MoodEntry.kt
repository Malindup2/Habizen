package com.example.habizen.data

import java.util.*

data class MoodEntry(
    val id: String = UUID.randomUUID().toString(),
    val emoji: String,
    val moodName: String,
    val note: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val dateString: String = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        .format(Date())
) {
    companion object {
        const val MOOD_VERY_SAD = "😢"
        const val MOOD_SAD = "😟"
        const val MOOD_NEUTRAL = "😐"
        const val MOOD_HAPPY = "🙂"
        const val MOOD_VERY_HAPPY = "😊"
        
        val MOOD_NAMES = mapOf(
            MOOD_VERY_SAD to "Very Sad",
            MOOD_SAD to "Sad",
            MOOD_NEUTRAL to "Neutral",
            MOOD_HAPPY to "Happy",
            MOOD_VERY_HAPPY to "Very Happy"
        )
        
        val MOOD_VALUES = mapOf(
            MOOD_VERY_SAD to 1,
            MOOD_SAD to 2,
            MOOD_NEUTRAL to 3,
            MOOD_HAPPY to 4,
            MOOD_VERY_HAPPY to 5
        )
    }
    
    fun getMoodValue(): Int {
        return MOOD_VALUES[emoji] ?: 3
    }
}
