package com.example.habizen.utils

import android.content.Context
import android.content.SharedPreferences
import com.example.habizen.data.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object PreferencesManager {
    private const val PREFS_NAME = "HabizenPrefs"
    
    // Keys
    private const val KEY_HABITS = "habits_list"
    private const val KEY_MOODS = "mood_entries"
    private const val KEY_HYDRATION_LOGS = "hydration_logs"
    private const val KEY_HYDRATION_SETTINGS = "hydration_settings"
    private const val KEY_USER_DATA = "user_data"
    private const val KEY_IS_LOGGED_IN = "is_logged_in"
    private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
    private const val KEY_THEME_MODE = "theme_mode"
    private const val KEY_FIRST_LAUNCH = "first_launch"
    
    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
    
    // Habits
    fun saveHabits(context: Context, habits: List<Habit>) {
        try {
            val json = Gson().toJson(habits)
            getPrefs(context).edit().putString(KEY_HABITS, json).apply()
        } catch (e: Exception) {
            e.printStackTrace()
            throw e // Re-throw to let calling code handle it
        }
    }
    
    fun getHabits(context: Context): List<Habit> {
        val json = getPrefs(context).getString(KEY_HABITS, "[]")
        val type = object : TypeToken<List<Habit>>() {}.type
        return try {
            Gson().fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    // Mood Entries
    fun saveMoodEntries(context: Context, moods: List<MoodEntry>) {
        val json = Gson().toJson(moods)
        getPrefs(context).edit().putString(KEY_MOODS, json).apply()
    }
    
    fun getMoodEntries(context: Context): List<MoodEntry> {
        val json = getPrefs(context).getString(KEY_MOODS, "[]")
        val type = object : TypeToken<List<MoodEntry>>() {}.type
        return try {
            Gson().fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    // Hydration Logs
    fun saveHydrationLogs(context: Context, logs: List<HydrationLog>) {
        val json = Gson().toJson(logs)
        getPrefs(context).edit().putString(KEY_HYDRATION_LOGS, json).apply()
    }
    
    fun getHydrationLogs(context: Context): List<HydrationLog> {
        val json = getPrefs(context).getString(KEY_HYDRATION_LOGS, "[]")
        val type = object : TypeToken<List<HydrationLog>>() {}.type
        return try {
            Gson().fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    // Hydration Entries (new methods for HydrationData)
    fun getHydrationEntries(context: Context): List<HydrationData> {
        val json = getPrefs(context).getString(KEY_HYDRATION_LOGS, "[]")
        val type = object : TypeToken<List<HydrationData>>() {}.type
        return try {
            Gson().fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    fun addHydrationEntry(context: Context, entry: HydrationData) {
        val entries = getHydrationEntries(context).toMutableList()
        entries.add(entry)
        val json = Gson().toJson(entries)
        getPrefs(context).edit().putString(KEY_HYDRATION_LOGS, json).apply()
    }
    
    fun deleteHydrationEntry(context: Context, entryId: String) {
        val entries = getHydrationEntries(context).toMutableList()
        entries.removeAll { it.id == entryId }
        val json = Gson().toJson(entries)
        getPrefs(context).edit().putString(KEY_HYDRATION_LOGS, json).apply()
    }
    
    fun getHydrationGoal(context: Context): Int {
        return getPrefs(context).getInt("hydration_goal", 2000)
    }
    
    fun setHydrationGoal(context: Context, goal: Int) {
        getPrefs(context).edit().putInt("hydration_goal", goal).apply()
    }
    
    // Hydration Settings
    fun saveHydrationSettings(context: Context, settings: HydrationSettings) {
        val json = Gson().toJson(settings)
        getPrefs(context).edit().putString(KEY_HYDRATION_SETTINGS, json).apply()
    }
    
    fun getHydrationSettings(context: Context): HydrationSettings {
        val json = getPrefs(context).getString(KEY_HYDRATION_SETTINGS, null)
        return if (json != null) {
            try {
                Gson().fromJson(json, HydrationSettings::class.java) ?: HydrationSettings()
            } catch (e: Exception) {
                HydrationSettings()
            }
        } else {
            HydrationSettings()
        }
    }
    
    // User Data
    fun saveUser(context: Context, user: User) {
        val json = Gson().toJson(user)
        getPrefs(context).edit().putString(KEY_USER_DATA, json).apply()
    }
    
    fun getUser(context: Context): User? {
        val json = getPrefs(context).getString(KEY_USER_DATA, null)
        return if (json != null) {
            try {
                Gson().fromJson(json, User::class.java)
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
    }
    
    // Authentication
    fun setLoggedIn(context: Context, isLoggedIn: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_IS_LOGGED_IN, isLoggedIn).apply()
    }
    
    fun isLoggedIn(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_IS_LOGGED_IN, false)
    }
    
    // Onboarding
    fun setOnboardingCompleted(context: Context, completed: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_ONBOARDING_COMPLETED, completed).apply()
    }
    
    fun isOnboardingCompleted(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_ONBOARDING_COMPLETED, false)
    }
    
    // First Launch
    fun setFirstLaunch(context: Context, isFirst: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_FIRST_LAUNCH, isFirst).apply()
    }
    
    fun isFirstLaunch(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_FIRST_LAUNCH, true)
    }
    
    // Theme
    fun setThemeMode(context: Context, mode: String) {
        getPrefs(context).edit().putString(KEY_THEME_MODE, mode).apply()
    }
    
    fun getThemeMode(context: Context): String {
        return getPrefs(context).getString(KEY_THEME_MODE, "system") ?: "system"
    }
    
    // Clear all data (for logout)
    fun clearAllData(context: Context) {
        getPrefs(context).edit().clear().apply()
    }
}
