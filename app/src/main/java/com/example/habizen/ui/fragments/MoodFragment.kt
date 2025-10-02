package com.example.habizen.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.habizen.R
import com.example.habizen.databinding.FragmentMoodBinding
import com.example.habizen.data.MoodEntry
import com.example.habizen.ui.adapters.MoodAdapter
import com.example.habizen.utils.PreferencesManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.applandeo.materialcalendarview.EventDay
import com.applandeo.materialcalendarview.listeners.OnDayClickListener
import java.util.*
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar

class MoodFragment : Fragment() {
    
    private var _binding: FragmentMoodBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var moodAdapter: MoodAdapter
    private var moodEntries = mutableListOf<MoodEntry>()
    private var selectedMood: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMoodBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        loadMoodEntries()
        setupClickListeners()
        setupCalendar()
    }
    
    private fun setupRecyclerView() {
        moodAdapter = MoodAdapter(moodEntries) { moodEntry ->
            showDeleteConfirmation(moodEntry)
        }
        
        binding.rvMoodEntries.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = moodAdapter
        }
    }
    
    private fun loadMoodEntries() {
        moodEntries.clear()
        moodEntries.addAll(PreferencesManager.getMoodEntries(requireContext()).sortedByDescending { it.timestamp })
        updateUI()
    }
    
    private fun setupClickListeners() {
        // Mood selection
        binding.tvMoodVerySad.setOnClickListener { selectMood(MoodEntry.MOOD_VERY_SAD, "Very Sad") }
        binding.tvMoodSad.setOnClickListener { selectMood(MoodEntry.MOOD_SAD, "Sad") }
        binding.tvMoodNeutral.setOnClickListener { selectMood(MoodEntry.MOOD_NEUTRAL, "Neutral") }
        binding.tvMoodHappy.setOnClickListener { selectMood(MoodEntry.MOOD_HAPPY, "Happy") }
        binding.tvMoodVeryHappy.setOnClickListener { selectMood(MoodEntry.MOOD_VERY_HAPPY, "Very Happy") }
        
        binding.btnSaveMood.setOnClickListener {
            saveMoodEntry()
        }

        binding.btnToggleView.setOnClickListener {
            toggleCalendar()
        }
    }

    private fun setupCalendar() {
        // Ensure calendar starts hidden; events will be loaded when toggled on
        binding.calendarView.setOnDayClickListener(object : OnDayClickListener {
            override fun onDayClick(eventDay: EventDay) {
                val clickedCal = eventDay.calendar
                val dateString = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    .format(clickedCal.time)

                // Find moods for this date
                val moodsForDate = PreferencesManager.getMoodEntries(requireContext())
                    .filter { it.dateString == dateString }

                if (moodsForDate.isEmpty()) {
                    // No mood on this date
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle("No entry")
                        .setMessage("No mood logged for this date.")
                        .setPositiveButton("OK", null)
                        .show()
                } else {
                    // Show dialog listing moods for that date
                    val sb = StringBuilder()
                    moodsForDate.forEach { m ->
                        val time = java.text.SimpleDateFormat("HH:mm", Locale.getDefault())
                            .format(Date(m.timestamp))
                        sb.append("${m.emoji} ${m.moodName} ($time)\n")
                        if (m.note.isNotEmpty()) sb.append("Note: ${m.note}\n")
                        sb.append("\n")
                    }

                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Mood entries")
                        .setMessage(sb.toString())
                        .setPositiveButton("OK", null)
                        .show()
                }
            }
        })
    }

    private fun toggleCalendar() {
        val show = binding.calendarView.visibility != View.VISIBLE
        if (show) {
            // Populate events
            loadCalendarEvents()
            binding.calendarView.visibility = View.VISIBLE
            binding.rvMoodEntries.visibility = View.GONE
            binding.llEmptyState.visibility = View.GONE
            binding.btnToggleView.text = "List View"
        } else {
            binding.calendarView.visibility = View.GONE
            binding.rvMoodEntries.visibility = if (moodEntries.isEmpty()) View.GONE else View.VISIBLE
            binding.llEmptyState.visibility = if (moodEntries.isEmpty()) View.VISIBLE else View.GONE
            binding.btnToggleView.text = "Calendar View"
        }
    }

    private fun loadCalendarEvents() {
        val events = mutableListOf<EventDay>()
        val moods = PreferencesManager.getMoodEntries(requireContext())

        // Create a representative drawable for each day's mood (use first entry of the day)
        val grouped = moods.groupBy { it.dateString }
        for ((dateStr, list) in grouped) {
            try {
                val cal = Calendar.getInstance()
                val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                cal.time = sdf.parse(dateStr) ?: Date()

                val mood = list.first()
                val drawable = createMoodDotDrawable(mood)
                events.add(EventDay(cal, drawable))
            } catch (e: Exception) {
                // ignore parse errors
            }
        }

        binding.calendarView.setEvents(events)
    }

    private fun createMoodDotDrawable(mood: com.example.habizen.data.MoodEntry): Drawable {
        // Return a small colored circle representing the mood value
        val colorRes = when (mood.getMoodValue()) {
            1 -> android.R.color.holo_blue_dark
            2 -> android.R.color.holo_blue_light
            3 -> android.R.color.darker_gray
            4 -> android.R.color.holo_orange_light
            5 -> android.R.color.holo_green_dark
            else -> android.R.color.darker_gray
        }

        val shape = android.graphics.drawable.GradientDrawable().apply {
            shape = android.graphics.drawable.GradientDrawable.OVAL
            setSize(24, 24)
            setColor(ContextCompat.getColor(requireContext(), colorRes))
        }

        return shape
    }
    
    private fun selectMood(emoji: String, name: String) {
        selectedMood = emoji
        binding.tvSelectedMood.text = "$emoji $name"
        
        // Reset background for all mood views
        resetMoodSelections()
        
        // Highlight selected mood
        when (emoji) {
            MoodEntry.MOOD_VERY_SAD -> binding.tvMoodVerySad.setBackgroundResource(android.R.color.holo_blue_light)
            MoodEntry.MOOD_SAD -> binding.tvMoodSad.setBackgroundResource(android.R.color.holo_blue_light)
            MoodEntry.MOOD_NEUTRAL -> binding.tvMoodNeutral.setBackgroundResource(android.R.color.holo_blue_light)
            MoodEntry.MOOD_HAPPY -> binding.tvMoodHappy.setBackgroundResource(android.R.color.holo_blue_light)
            MoodEntry.MOOD_VERY_HAPPY -> binding.tvMoodVeryHappy.setBackgroundResource(android.R.color.holo_blue_light)
        }
    }
    
    private fun resetMoodSelections() {
        binding.tvMoodVerySad.background = null
        binding.tvMoodSad.background = null
        binding.tvMoodNeutral.background = null
        binding.tvMoodHappy.background = null
        binding.tvMoodVeryHappy.background = null
    }
    
    private fun saveMoodEntry() {
        if (selectedMood == null) {
            Snackbar.make(binding.root, "Please select your mood first", Snackbar.LENGTH_SHORT).show()
            return
        }
        
        val note = binding.etMoodNote.text.toString().trim()
        val moodName = MoodEntry.MOOD_NAMES[selectedMood] ?: "Unknown"
        
        val moodEntry = MoodEntry(
            emoji = selectedMood!!,
            moodName = moodName,
            note = note
        )
        
        moodEntries.add(0, moodEntry) // Add to beginning of list
        saveMoodEntries()
        updateUI()
        
        // Reset form
        resetForm()
        
        Snackbar.make(binding.root, "Mood saved successfully!", Snackbar.LENGTH_SHORT).show()
    }
    
    private fun resetForm() {
        selectedMood = null
        binding.tvSelectedMood.text = "Select your mood"
        binding.etMoodNote.text?.clear()
        resetMoodSelections()
    }
    
    private fun showDeleteConfirmation(moodEntry: MoodEntry) {
        MaterialAlertDialogBuilder(requireContext(), R.style.CustomDeleteDialog)
            .setTitle("Delete Mood Entry")
            .setMessage("Are you sure you want to delete this mood entry?")
            .setPositiveButton("Delete") { _, _ ->
                deleteMoodEntry(moodEntry)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun deleteMoodEntry(moodEntry: MoodEntry) {
        moodEntries.removeAll { it.id == moodEntry.id }
        saveMoodEntries()
        updateUI()
        
        Snackbar.make(binding.root, "Mood entry deleted", Snackbar.LENGTH_SHORT).show()
    }
    
    private fun updateUI() {
        if (moodEntries.isEmpty()) {
            binding.llEmptyState.visibility = View.VISIBLE
            binding.rvMoodEntries.visibility = View.GONE
        } else {
            binding.llEmptyState.visibility = View.GONE
            binding.rvMoodEntries.visibility = View.VISIBLE
        }
        
        moodAdapter.notifyDataSetChanged()
    }
    
    private fun saveMoodEntries() {
        PreferencesManager.saveMoodEntries(requireContext(), moodEntries)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
