package com.example.habizen.data

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val emoji: String,
    val isUnlocked: Boolean,
    val progress: Int = 0,
    val target: Int = 0
)
