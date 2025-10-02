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

class EditHabitDialog(
    private val habit: Habit,
    private val onHabitUpdated: (Habit) -> Unit
) : DialogFragment() {

    private var _binding: DialogAddHabitBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var emojiAdapter: EmojiAdapter
    private var selectedEmoji = habit.emoji
    
    private val emojis = listOf(
        "💧", "🏃", "📚", "🧘", "🥗", "😴", "💪", "🎯",
        "☀️", "🌙", "🚶", "🍎", "🧠", "❤️", "🌱", "⭐",
        "🎨", "🎵", "📝", "🔥", "⚡", "🌟", "🏆", "✨"
    )
    
    private val categories = listOf(
        "General", "Health", "Fitness", "Learning", "Mindfulness", 
        "Nutrition", "Sleep", "Work", "Personal Growth"
    )

    private var reminderEnabled = habit.reminderEnabled
    private var reminderTime = habit.reminderTime

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogAddHabitBinding.inflate(layoutInflater)
        
        setupEmojiPicker()
        setupCategorySpinner()
        populateFields()
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
    
    private fun populateFields() {
        binding.etHabitName.setText(habit.name)
        binding.etGoal.setText(habit.goal.toString())
        binding.tvSelectedEmoji.text = "Selected: ${habit.emoji}"
        
        val categoryIndex = categories.indexOf(habit.category)
        if (categoryIndex != -1) {
            binding.spinnerCategory.setSelection(categoryIndex)
        }
        
        binding.btnAdd.text = "Update Habit"
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
                updateHabit()
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
    
    private fun updateHabit() {
        val updatedHabit = habit.copy(
            name = binding.etHabitName.text.toString().trim(),
            emoji = selectedEmoji,
            goal = binding.etGoal.text.toString().toInt(),
            category = categories[binding.spinnerCategory.selectedItemPosition],
            reminderEnabled = reminderEnabled,
            reminderTime = reminderTime
        )
        
        onHabitUpdated(updatedHabit)
        dismiss()
    }

    private fun updateReminderViews() {
        binding.switchReminder.isChecked = reminderEnabled
        binding.containerReminderTime.visibility = if (reminderEnabled) View.VISIBLE else View.GONE
        binding.tvReminderTime.text = reminderTime
    }

    private fun showTimePicker() {
        val (hour, minute) = reminderTime.split(":").map { it.toInt() }
        TimePickerDialog(requireContext(), { _, selectedHour, selectedMinute ->
            reminderTime = String.format("%02d:%02d", selectedHour, selectedMinute)
            updateReminderViews()
        }, hour, minute, true).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
