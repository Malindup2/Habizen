package com.example.habizen.ui.dialogs

import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.habizen.databinding.DialogAddHabitBinding
import com.example.habizen.data.Habit
import com.example.habizen.ui.adapters.EmojiAdapter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.util.Calendar

class AddHabitDialog(
    private val onHabitAdded: (Habit) -> Unit
) : DialogFragment() {

    private var _binding: DialogAddHabitBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var emojiAdapter: EmojiAdapter
    private var selectedEmoji = "🎯"
    
    private val emojis = listOf(
        "💧", "🏃", "📚", "🧘", "🥗", "😴", "💪", "🎯",
        "☀️", "🌙", "🚶", "🍎", "🧠", "❤️", "🌱", "⭐",
        "🎨", "🎵", "📝", "🔥", "⚡", "🌟", "🏆", "✨"
    )
    
    private val categories = listOf(
        "General", "Health", "Fitness", "Learning", "Mindfulness", 
        "Nutrition", "Sleep", "Work", "Personal Growth"
    )

    private var reminderEnabled = false
    private var reminderTime = "08:00"

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogAddHabitBinding.inflate(layoutInflater)
        
        setupEmojiPicker()
        setupCategorySpinner()
        setupClickListeners()
        updateReminderViews()
        
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(binding.root)
            .create()
            
        // Set white background and ensure proper sizing
        dialog.window?.setBackgroundDrawableResource(android.R.color.white)
        dialog.window?.setLayout(
            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        )
        
        return dialog
    }
    
    private fun setupEmojiPicker() {
        emojiAdapter = EmojiAdapter(emojis) { emoji ->
            selectedEmoji = emoji
            binding.tvSelectedEmoji.text = "Selected: $emoji"
        }
        
        binding.rvEmojis.apply {
            layoutManager = GridLayoutManager(context, 6)
            adapter = emojiAdapter
        }
    }
    
    private fun setupCategorySpinner() {
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            categories
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCategory.adapter = adapter
    }
    
    private fun setupClickListeners() {
        binding.btnCancel.setOnClickListener {
            dismiss()
        }
        
        binding.btnAdd.setOnClickListener {
            if (validateInput()) {
                createHabit()
            }
        }

        binding.switchReminder.setOnCheckedChangeListener { _, isChecked ->
            reminderEnabled = isChecked
            updateReminderViews()
        }

        binding.btnPickReminderTime.setOnClickListener {
            showTimePicker()
        }
    }
    
    private fun validateInput(): Boolean {
        val name = binding.etHabitName.text.toString().trim()
        val goal = binding.etGoal.text.toString().trim()
        
        var isValid = true
        
        if (name.isEmpty()) {
            binding.tilHabitName.error = "Habit name is required"
            isValid = false
        } else {
            binding.tilHabitName.error = null
        }
        
        if (goal.isEmpty()) {
            binding.tilGoal.error = "Goal is required"
            isValid = false
        } else if (goal.toIntOrNull() == null || goal.toInt() <= 0) {
            binding.tilGoal.error = "Goal must be a positive number"
            isValid = false
        } else {
            binding.tilGoal.error = null
        }
        
        return isValid
    }
    
    private fun createHabit() {
        try {
            val habit = Habit(
                name = binding.etHabitName.text.toString().trim(),
                emoji = selectedEmoji,
                goal = binding.etGoal.text.toString().toInt(),
                category = categories[binding.spinnerCategory.selectedItemPosition],
                reminderEnabled = reminderEnabled,
                reminderTime = reminderTime
            )
            
            onHabitAdded(habit)
            dismiss()
        } catch (e: Exception) {
            e.printStackTrace()
            // Show error message to user
            binding.tilHabitName.error = "Error creating habit. Please try again."
        }
    }

    private fun updateReminderViews() {
        binding.containerReminderTime.visibility = if (reminderEnabled) View.VISIBLE else View.GONE
        binding.tvReminderTime.text = reminderTime
    }

    private fun showTimePicker() {
        val currentTime = try {
            val parts = reminderTime.split(":")
            if (parts.size >= 2) {
                Pair(parts[0].toInt(), parts[1].toInt())
            } else {
                Pair(8, 0) // Default to 8:00 AM
            }
        } catch (e: Exception) {
            Pair(8, 0) // Default to 8:00 AM if parsing fails
        }
        
        TimePickerDialog(requireContext(), { _, selectedHour, selectedMinute ->
            reminderTime = String.format("%02d:%02d", selectedHour, selectedMinute)
            updateReminderViews()
        }, currentTime.first, currentTime.second, true).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
