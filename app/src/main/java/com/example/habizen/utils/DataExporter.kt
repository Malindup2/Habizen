package com.example.habizen.utils

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.habizen.data.Habit
import com.example.habizen.data.HydrationData
import com.example.habizen.data.MoodEntry
import com.google.gson.Gson
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Locale

object DataExporter {

    fun exportJson(context: Context): Uri? {
        val bundle = collectData(context)
        if (bundle.isEmpty()) return null
        val json = Gson().toJson(bundle)
        return writeToFile(context, "habizen_export.json", json)
    }

    fun exportCsv(context: Context): Uri? {
        val bundle = collectData(context)
        if (bundle.isEmpty()) return null

        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val builder = StringBuilder()

        builder.appendLine("Habits")
        builder.appendLine("id,name,emoji,goal,category,reminderEnabled,reminderTime,completionCount")
        bundle.habits.forEach { habit ->
            builder.appendLine(listOf(
                habit.id,
                habit.name,
                habit.emoji,
                habit.goal,
                habit.category,
                habit.reminderEnabled,
                habit.reminderTime,
                habit.completionDates.size
            ).joinToString(separator = ",") { escapeCsv(it) })
        }
        builder.appendLine()

        builder.appendLine("Mood Entries")
        builder.appendLine("id,emoji,mood,note,date")
        bundle.moods.forEach { mood ->
            builder.appendLine(listOf(
                mood.id,
                mood.emoji,
                mood.moodName,
                mood.note,
                formatter.format(mood.timestamp)
            ).joinToString(separator = ",") { escapeCsv(it) })
        }
        builder.appendLine()

        builder.appendLine("Hydration")
        builder.appendLine("id,amount,logged_at,date")
        bundle.hydration.forEach { hydration ->
            builder.appendLine(listOf(
                hydration.id,
                hydration.amount,
                hydration.time,
                hydration.date
            ).joinToString(separator = ",") { escapeCsv(it) })
        }

        return writeToFile(context, "habizen_export.csv", builder.toString())
    }

    private fun collectData(context: Context): ExportBundle {
        val habits = PreferencesManager.getHabits(context)
        val moods = PreferencesManager.getMoodEntries(context)
        val hydration = PreferencesManager.getHydrationEntries(context)
        return ExportBundle(habits, moods, hydration)
    }

    private fun writeToFile(context: Context, fileName: String, content: String): Uri {
        val exportDir = File(context.cacheDir, "exports").apply { if (!exists()) mkdirs() }
        val file = File(exportDir, fileName)
        FileWriter(file).use { writer ->
            writer.write(content)
        }
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    private fun ExportBundle.isEmpty(): Boolean {
        return habits.isEmpty() && moods.isEmpty() && hydration.isEmpty()
    }

    private fun escapeCsv(value: Any?): String {
        val text = value?.toString() ?: ""
        val needsQuotes = text.contains(',') || text.contains('\n') || text.contains('"')
        val escaped = text.replace("\"", "\"\"")
        return if (needsQuotes) "\"$escaped\"" else escaped
    }

    private data class ExportBundle(
        val habits: List<Habit>,
        val moods: List<MoodEntry>,
        val hydration: List<HydrationData>
    )
}
