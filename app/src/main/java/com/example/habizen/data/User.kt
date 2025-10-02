package com.example.habizen.data

data class User(
    val name: String,
    val email: String,
    val password: String, // Simple hash for demo
    val joinDate: Long = System.currentTimeMillis(),
    val profileImagePath: String = ""
)
