package com.example.habizen.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.habizen.databinding.FragmentHabitsBinding
import com.example.habizen.data.Habit
import com.example.habizen.ui.adapters.HabitsAdapter
import com.example.habizen.ui.dialogs.AddHabitDialog
import com.example.habizen.ui.dialogs.EditHabitDialog
import com.example.habizen.utils.HabitReminderScheduler
import com.example.habizen.utils.PreferencesManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.text.SimpleDateFormat
import java.util.*

class HabitsFragment : Fragment() {
    
    private var _binding: FragmentHabitsBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var habitsAdapter: HabitsAdapter
    private var habits = mutableListOf<Habit>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHabitsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupUI()
        setupRecyclerView()
        loadHabits()
        setupClickListeners()
    }
    
    private fun setupUI() {
        val dateFormat = SimpleDateFormat("EEEE, MMMM dd", Locale.getDefault())
        binding.tvCurrentDate.text = dateFormat.format(Date())
    }
    
    private fun setupRecyclerView() {
        habitsAdapter = HabitsAdapter(
            habits = habits,
            onHabitChecked = { habit, isChecked ->
                handleHabitChecked(habit, isChecked)
            },
            onEditClicked = { habit ->
                showEditHabitDialog(habit)
            },
            onDeleteClicked = { habit ->
                showDeleteConfirmation(habit)
            }
        )
        
        binding.rvHabits.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = habitsAdapter
        }
    }
    
    private fun loadHabits() {
        habits.clear()
        habits.addAll(PreferencesManager.getHabits(requireContext()))
        habits.filter { it.reminderEnabled }
            .forEach { HabitReminderScheduler.scheduleHabitReminder(requireContext(), it) }
        updateUI()
    }
    
    private fun setupClickListeners() {
        binding.tvAddHabit.setOnClickListener {
            showAddHabitDialog()
        }
        
        binding.btnAddFirstHabit.setOnClickListener {
            showAddHabitDialog()
        }
        
        // FAB click is handled in MainActivity
        activity?.findViewById<View>(com.example.habizen.R.id.fabAddHabit)?.setOnClickListener {
            showAddHabitDialog()
        }
    }
    
    private fun updateUI() {
        if (habits.isEmpty()) {
            binding.llEmptyState.visibility = View.VISIBLE
            binding.rvHabits.visibility = View.GONE
            binding.progressIndicator.progress = 0
            binding.tvProgressPercentage.text = "0%"
            binding.tvProgressDescription.text = "No habits to track"
        } else {
            binding.llEmptyState.visibility = View.GONE
            binding.rvHabits.visibility = View.VISIBLE
            updateProgress()
        }
        
        habitsAdapter.notifyDataSetChanged()
    }
    
    private fun updateProgress() {
        val completedHabits = habits.count { it.isCompletedToday() }
        val totalHabits = habits.size
        val percentage = if (totalHabits > 0) {
            (completedHabits.toFloat() / totalHabits * 100).toInt()
        } else {
            0
        }
        
        binding.progressIndicator.progress = percentage
        binding.tvProgressPercentage.text = "${percentage}%"
        binding.tvProgressDescription.text = "$completedHabits of $totalHabits habits completed"
    }
    
    private fun handleHabitChecked(habit: Habit, isChecked: Boolean) {
        val updatedHabit = if (isChecked) {
            habit.copy().apply { markCompleted() }
        } else {
            // Remove today's completion
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            habit.copy().apply { 
                completionDates.remove(today)
            }
        }
        
        updateHabit(updatedHabit)
        
        if (isChecked && updatedHabit.isCompletedToday()) {
            // Show completion celebration
            showCompletionDialog(habit.name)
        }
    }
    
    fun showAddHabitDialogFromExternal() {
        showAddHabitDialog()
    }
    
    private fun showAddHabitDialog() {
        val dialog = AddHabitDialog { habit ->
            addHabit(habit)
        }
        dialog.show(parentFragmentManager, "AddHabitDialog")
    }
    
    private fun showEditHabitDialog(habit: Habit) {
        val dialog = EditHabitDialog(habit) { updatedHabit ->
            updateHabit(updatedHabit)
        }
        dialog.show(parentFragmentManager, "EditHabitDialog")
    }
    
    private fun showDeleteConfirmation(habit: Habit) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Habit")
            .setMessage("Are you sure you want to delete \"${habit.name}\"? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                deleteHabit(habit)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showCompletionDialog(habitName: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("🎉 Congratulations!")
            .setMessage("You've completed your \"$habitName\" habit for today. Keep up the great work!")
            .setPositiveButton("Awesome!") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
    
    private fun addHabit(habit: Habit) {
        try {
            habits.add(habit)
            saveHabits()
            if (habit.reminderEnabled) {
                HabitReminderScheduler.scheduleHabitReminder(requireContext(), habit)
            }
            updateUI()
        } catch (e: Exception) {
            e.printStackTrace()
            // Remove the habit from the list if saving failed
            habits.removeAll { it.id == habit.id }
            // Show error message to user
            com.google.android.material.snackbar.Snackbar.make(
                binding.root,
                "Failed to add habit. Please try again.",
                com.google.android.material.snackbar.Snackbar.LENGTH_LONG
            ).show()
        }
    }
    
    private fun updateHabit(updatedHabit: Habit) {
        val index = habits.indexOfFirst { it.id == updatedHabit.id }
        if (index != -1) {
            habits[index] = updatedHabit
            saveHabits()
            if (updatedHabit.reminderEnabled) {
                HabitReminderScheduler.scheduleHabitReminder(requireContext(), updatedHabit)
            } else {
                HabitReminderScheduler.cancelHabitReminder(requireContext(), updatedHabit.id)
            }
            updateUI()
        }
    }
    
    private fun deleteHabit(habit: Habit) {
        habits.removeAll { it.id == habit.id }
        saveHabits()
        HabitReminderScheduler.cancelHabitReminder(requireContext(), habit.id)
        updateUI()
    }
    
    private fun saveHabits() {
        PreferencesManager.saveHabits(requireContext(), habits)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
