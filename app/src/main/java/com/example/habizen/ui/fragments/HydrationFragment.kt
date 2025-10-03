package com.example.habizen.ui.fragments

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.NotificationManager
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.*
import com.example.habizen.R
import com.example.habizen.data.HydrationData
import com.example.habizen.databinding.FragmentHydrationBinding
import com.example.habizen.utils.PreferencesManager
import com.example.habizen.utils.GoalCompletionNotificationManager
import com.example.habizen.workers.HydrationReminderWorker
import com.google.android.material.button.MaterialButton
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class HydrationFragment : Fragment() {
    
    private var _binding: FragmentHydrationBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var hydrationAdapter: HydrationAdapter
    private var dailyGoal = 2350 // Default 2.35L = 2350ml
    private var currentIntake = 0
    private var drinksCount = 0
    private var currentStreak = 0
    
    // Handler for button animation reset
    private val animationHandler = Handler(Looper.getMainLooper())
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHydrationBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupUI()
        setupRecyclerView()
        loadHydrationData()
        setupWorkManager()
        loadWeeklyData()
    }
    
    private fun setupUI() {
        // Drink type buttons with click animation
        binding.btnWaterGlass.setOnClickListener {
            animateButtonClick(binding.btnWaterGlass as MaterialButton)
            addWater(250, "Water Glass")
        }
        binding.btnCoffee.setOnClickListener {
            animateButtonClick(binding.btnCoffee as MaterialButton)
            addWater(200, "Coffee")
        }
        binding.btnTea.setOnClickListener {
            animateButtonClick(binding.btnTea as MaterialButton)
            addWater(200, "Tea")
        }
        binding.btnJuice.setOnClickListener {
            animateButtonClick(binding.btnJuice as MaterialButton)
            addWater(200, "Juice")
        }
        binding.btnSoda.setOnClickListener {
            animateButtonClick(binding.btnSoda as MaterialButton)
            addWater(330, "Soda")
        }
        binding.btnEnergyDrink.setOnClickListener {
            animateButtonClick(binding.btnEnergyDrink as MaterialButton)
            addWater(200, "Energy Drink")
        }
        
        // Custom amount
        binding.btnAddCustom.setOnClickListener {
            val amount = binding.etCustomAmount.text.toString().toIntOrNull()
            if (amount != null && amount > 0) {
                addWater(amount, "Custom")
                binding.etCustomAmount.text?.clear()
                // Card stays visible now
            } else {
                Toast.makeText(requireContext(), "Please enter a valid amount", Toast.LENGTH_SHORT).show()
            }
        }
        
        // Settings button
        binding.btnSettings.setOnClickListener {
            showSettingsDialog()
        }
    }
    
    private fun setupRecyclerView() {
        hydrationAdapter = HydrationAdapter(
            onDeleteClick = { hydrationData ->
                deleteHydrationEntry(hydrationData)
            }
        )
        
        binding.rvHydrationHistory.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = hydrationAdapter
            isNestedScrollingEnabled = true
        }
    }
    
    private fun loadHydrationData() {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val todayEntries = PreferencesManager.getHydrationEntries(requireContext()).filter { it.date == today }
        
        currentIntake = todayEntries.sumOf { it.amount }
        dailyGoal = PreferencesManager.getHydrationGoal(requireContext())
        
        updateUI()
        
        // Load all entries for history
        val allEntries = PreferencesManager.getHydrationEntries(requireContext()).sortedByDescending { 
            SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).parse("${it.date} ${it.time}")
        }
        hydrationAdapter.updateEntries(allEntries)
        
        // Show/hide empty state
        if (allEntries.isEmpty()) {
            binding.llEmptyHistory.visibility = View.VISIBLE
            binding.rvHydrationHistory.visibility = View.GONE
        } else {
            binding.llEmptyHistory.visibility = View.GONE
            binding.rvHydrationHistory.visibility = View.VISIBLE
        }
    }
    
    private fun addWater(amount: Int, drinkType: String = "Water") {
        val currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        
        val hydrationEntry = HydrationData(
            id = System.currentTimeMillis().toString(),
            amount = amount,
            time = currentTime,
            date = currentDate,
            drinkType = drinkType
        )
        
        PreferencesManager.addHydrationEntry(requireContext(), hydrationEntry)
        
        currentIntake += amount
        drinksCount++
        updateUI()
        loadHydrationData()
        loadWeeklyData()
        
        // Check if goal reached
        if (currentIntake >= dailyGoal) {
            Toast.makeText(requireContext(), "🎉 Daily hydration goal achieved!", Toast.LENGTH_LONG).show()
            // Send goal completion notification
            GoalCompletionNotificationManager.sendHydrationGoalNotification(requireContext(), dailyGoal)
        }
    }
    
    private fun loadWeeklyData() {
        val calendar = Calendar.getInstance()
        val today = calendar.get(Calendar.DAY_OF_WEEK)
        
        // Calculate the start of the week (Monday)
        val daysToSubtract = if (today == Calendar.SUNDAY) 6 else today - Calendar.MONDAY
        calendar.add(Calendar.DAY_OF_WEEK, -daysToSubtract)
        
        val weekDays = mutableListOf<String>()
        for (i in 0..6) {
            weekDays.add(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time))
            calendar.add(Calendar.DAY_OF_WEEK, 1)
        }
        
        // Load data for each day
        val mondayAmount = PreferencesManager.getHydrationEntries(requireContext())
            .filter { it.date == weekDays[0] }.sumOf { it.amount }
        val tuesdayAmount = PreferencesManager.getHydrationEntries(requireContext())
            .filter { it.date == weekDays[1] }.sumOf { it.amount }
        val wednesdayAmount = PreferencesManager.getHydrationEntries(requireContext())
            .filter { it.date == weekDays[2] }.sumOf { it.amount }
        val thursdayAmount = PreferencesManager.getHydrationEntries(requireContext())
            .filter { it.date == weekDays[3] }.sumOf { it.amount }
        val fridayAmount = PreferencesManager.getHydrationEntries(requireContext())
            .filter { it.date == weekDays[4] }.sumOf { it.amount }
        val saturdayAmount = PreferencesManager.getHydrationEntries(requireContext())
            .filter { it.date == weekDays[5] }.sumOf { it.amount }
        val sundayAmount = PreferencesManager.getHydrationEntries(requireContext())
            .filter { it.date == weekDays[6] }.sumOf { it.amount }
        
        // Update progress bars
        updateWeeklyProgress(mondayAmount, tuesdayAmount, wednesdayAmount, thursdayAmount, 
                           fridayAmount, saturdayAmount, sundayAmount)
    }
    
    private fun updateWeeklyProgress(mon: Int, tue: Int, wed: Int, thu: Int, fri: Int, sat: Int, sun: Int) {
        val goal = dailyGoal
        
        binding.progressMonday.progress = ((mon.toFloat() / goal) * 100).toInt().coerceAtMost(100)
        binding.tvMondayAmount.text = "${mon}ml"
        
        binding.progressTuesday.progress = ((tue.toFloat() / goal) * 100).toInt().coerceAtMost(100)
        binding.tvTuesdayAmount.text = "${tue}ml"
        
        binding.progressWednesday.progress = ((wed.toFloat() / goal) * 100).toInt().coerceAtMost(100)
        binding.tvWednesdayAmount.text = "${wed}ml"
        
        binding.progressThursday.progress = ((thu.toFloat() / goal) * 100).toInt().coerceAtMost(100)
        binding.tvThursdayAmount.text = "${thu}ml"
        
        binding.progressFriday.progress = ((fri.toFloat() / goal) * 100).toInt().coerceAtMost(100)
        binding.tvFridayAmount.text = "${fri}ml"
        
        binding.progressSaturday.progress = ((sat.toFloat() / goal) * 100).toInt().coerceAtMost(100)
        binding.tvSaturdayAmount.text = "${sat}ml"
        
        binding.progressSunday.progress = ((sun.toFloat() / goal) * 100).toInt().coerceAtMost(100)
        binding.tvSundayAmount.text = "${sun}ml"
    }
    
    private fun deleteHydrationEntry(hydrationData: HydrationData) {
        PreferencesManager.deleteHydrationEntry(requireContext(), hydrationData.id)
        
        // Recalculate today's intake and drinks count
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        if (hydrationData.date == today) {
            currentIntake -= hydrationData.amount
            drinksCount--
            updateUI()
        }
        
        loadHydrationData()
        loadWeeklyData()
        Toast.makeText(requireContext(), "Entry deleted", Toast.LENGTH_SHORT).show()
    }
    
    private fun updateUI() {
        val percentage = (currentIntake.toFloat() / dailyGoal * 100).coerceAtMost(100f)
        
        binding.apply {
            tvCurrentIntake.text = "${currentIntake}ml"
            tvDailyGoal.text = "of ${dailyGoal}ml"
            progressHydration.progress = percentage.toInt()
            tvProgress.text = "${percentage.toInt()}% Complete"
            
            // Update today's stats
            tvDrinksCount.text = drinksCount.toString()
            tvStreakCount.text = calculateStreak().toString()
            
            val remaining = dailyGoal - currentIntake
            tvRemainingAmount.text = if (remaining > 0) "${remaining}ml" else "0ml"
        }
    }
    
    private fun calculateStreak(): Int {
        // Simple streak calculation - count consecutive days with goal achievement
        val calendar = Calendar.getInstance()
        var streak = 0
        var checkDate = calendar.time
        
        while (true) {
            val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(checkDate)
            val dayEntries = PreferencesManager.getHydrationEntries(requireContext())
                .filter { it.date == dateStr }
            val dayTotal = dayEntries.sumOf { it.amount }
            
            if (dayTotal >= dailyGoal) {
                streak++
                calendar.add(Calendar.DAY_OF_MONTH, -1)
                checkDate = calendar.time
            } else {
                break
            }
        }
        
        return streak
    }
    
    private fun showSettingsDialog() {
        val dialog = HydrationSettingsDialog(
            currentGoal = dailyGoal,
            onGoalSet = { newGoal ->
                dailyGoal = newGoal
                PreferencesManager.setHydrationGoal(requireContext(), newGoal)
                updateUI()
                setupWorkManager() // Restart reminders with new settings
            }
        )
        dialog.show(parentFragmentManager, "HydrationSettings")
    }
    
    private fun setupWorkManager() {
        // Cancel existing work
        WorkManager.getInstance(requireContext()).cancelUniqueWork("hydration_reminder")
        
        // Create periodic reminder (every 2 hours during day)
        val reminderRequest = PeriodicWorkRequestBuilder<HydrationReminderWorker>(2, TimeUnit.HOURS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                    .build()
            )
            .build()
        
        WorkManager.getInstance(requireContext()).enqueueUniquePeriodicWork(
            "hydration_reminder",
            ExistingPeriodicWorkPolicy.REPLACE,
            reminderRequest
        )
    }
    
    private fun animateButtonClick(button: MaterialButton) {
        // Cancel any pending animation reset
        animationHandler.removeCallbacksAndMessages(null)
        
        // Change to active state (filled button with primary background, white text/icons)
        button.setBackgroundColor(resources.getColor(R.color.primary, null))
        button.setTextColor(resources.getColor(R.color.white, null))
        button.setIconTintResource(R.color.white)
        button.strokeColor = null // Remove stroke for filled appearance
        
        // Scale animation
        val scaleX = ObjectAnimator.ofFloat(button, "scaleX", 1f, 0.95f, 1f)
        val scaleY = ObjectAnimator.ofFloat(button, "scaleY", 1f, 0.95f, 1f)
        
        val animatorSet = AnimatorSet()
        animatorSet.playTogether(scaleX, scaleY)
        animatorSet.duration = 150
        animatorSet.start()
        
        // Revert to idle state after 300ms (outlined button style)
        animationHandler.postDelayed({
            // Reset to outlined button style
            button.backgroundTintList = resources.getColorStateList(R.color.white, null)
            button.setTextColor(resources.getColor(R.color.primary, null))
            button.setIconTintResource(R.color.primary)
            button.strokeColor = resources.getColorStateList(R.color.primary, null)
        }, 300)
    }
    
    override fun onResume() {
        super.onResume()
        loadHydrationData() // Refresh data when returning to fragment
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
