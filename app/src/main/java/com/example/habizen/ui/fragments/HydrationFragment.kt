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
    }
    
    private fun setupUI() {
        // Quick add buttons with click animation
        binding.add250ml.setOnClickListener {
            animateButtonClick(binding.add250ml as MaterialButton)
            addWater(250)
        }
        binding.add500ml.setOnClickListener {
            animateButtonClick(binding.add500ml as MaterialButton)
            addWater(500)
        }
        binding.add750ml.setOnClickListener {
            animateButtonClick(binding.add750ml as MaterialButton)
            addWater(750)
        }
        
        // Custom amount
        binding.btnAddCustom.setOnClickListener {
            val amount = binding.etCustomAmount.text.toString().toIntOrNull()
            if (amount != null && amount > 0) {
                addWater(amount)
                binding.etCustomAmount.text?.clear()
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
    
    private fun addWater(amount: Int) {
        val currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        
        val hydrationEntry = HydrationData(
            id = System.currentTimeMillis().toString(),
            amount = amount,
            time = currentTime,
            date = currentDate
        )
        
        PreferencesManager.addHydrationEntry(requireContext(), hydrationEntry)
        
        currentIntake += amount
        updateUI()
        loadHydrationData()
        
        // Check if goal reached
        if (currentIntake >= dailyGoal) {
            Toast.makeText(requireContext(), "🎉 Daily hydration goal achieved!", Toast.LENGTH_LONG).show()
        }
    }
    
    private fun deleteHydrationEntry(hydrationData: HydrationData) {
        PreferencesManager.deleteHydrationEntry(requireContext(), hydrationData.id)
        
        // Recalculate today's intake
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        if (hydrationData.date == today) {
            currentIntake -= hydrationData.amount
            updateUI()
        }
        
        loadHydrationData() // This will also update the empty state
        Toast.makeText(requireContext(), "Entry deleted", Toast.LENGTH_SHORT).show()
    }
    
    private fun updateUI() {
        val percentage = (currentIntake.toFloat() / dailyGoal * 100).coerceAtMost(100f)
        
        binding.apply {
            tvCurrentIntake.text = "${currentIntake}ml"
            tvDailyGoal.text = "Goal: ${dailyGoal}ml"
            progressHydration.progress = percentage.toInt()
            
            // Show remaining amount or completion status
            val remaining = dailyGoal - currentIntake
            tvProgress.text = if (percentage >= 100f) {
                "🎉 Goal Achieved!"
            } else {
                "${percentage.toInt()}% • ${remaining}ml left"
            }
            
            // Animate water drops based on progress with visual effects
            animateWaterDrops(percentage)
        }
    }
    
    private fun animateWaterDrops(percentage: Float) {
        val filledDrops = (percentage / 20).toInt() // 5 drops = 100%
        val dropViews = listOf(binding.drop1, binding.drop2, binding.drop3, binding.drop4, binding.drop5)
        
        dropViews.forEachIndexed { index, imageView ->
            val shouldBeFilled = index < filledDrops
            
            // Set the appropriate drawable
            imageView.setImageResource(
                if (shouldBeFilled) R.drawable.ic_water_drop_filled 
                else R.drawable.ic_water_drop_outline
            )
            
            // Animate alpha and scale for visual feedback
            val targetAlpha = if (shouldBeFilled) 1.0f else 0.6f
            val targetScale = if (shouldBeFilled) 1.1f else 1.0f
            
            // Alpha animation
            val alphaAnimator = ObjectAnimator.ofFloat(imageView, "alpha", imageView.alpha, targetAlpha)
            alphaAnimator.duration = 300
            
            // Scale animation
            val scaleX = ObjectAnimator.ofFloat(imageView, "scaleX", imageView.scaleX, targetScale)
            val scaleY = ObjectAnimator.ofFloat(imageView, "scaleY", imageView.scaleY, targetScale)
            
            val scaleAnimatorSet = AnimatorSet()
            scaleAnimatorSet.playTogether(scaleX, scaleY)
            scaleAnimatorSet.duration = 300
            
            // Play animations together
            val combinedAnimator = AnimatorSet()
            combinedAnimator.playTogether(alphaAnimator, scaleAnimatorSet)
            combinedAnimator.start()
            
            // Add ripple effect for filled drops
            if (shouldBeFilled && imageView.tag != "filled") {
                imageView.tag = "filled"
                // Subtle bounce effect for newly filled drops
                val bounceScaleX = ObjectAnimator.ofFloat(imageView, "scaleX", 1.0f, 1.3f, 1.1f)
                val bounceScaleY = ObjectAnimator.ofFloat(imageView, "scaleY", 1.0f, 1.3f, 1.1f)
                
                val bounceSet = AnimatorSet()
                bounceSet.playTogether(bounceScaleX, bounceScaleY)
                bounceSet.duration = 400
                bounceSet.interpolator = android.view.animation.BounceInterpolator()
                bounceSet.start()
            } else if (!shouldBeFilled) {
                imageView.tag = null
            }
        }
        
        // Special celebration when all drops are filled
        if (filledDrops == 5 && percentage >= 100f) {
            celebrateGoalAchieved()
        }
    }
    
    private fun celebrateGoalAchieved() {
        val dropViews = listOf(binding.drop1, binding.drop2, binding.drop3, binding.drop4, binding.drop5)
        
        // Create a wave effect across all drops
        dropViews.forEachIndexed { index, imageView ->
            Handler(Looper.getMainLooper()).postDelayed({
                // Celebration bounce
                val celebrationScaleX = ObjectAnimator.ofFloat(imageView, "scaleX", 1.1f, 1.4f, 1.1f)
                val celebrationScaleY = ObjectAnimator.ofFloat(imageView, "scaleY", 1.1f, 1.4f, 1.1f)
                val celebrationAlpha = ObjectAnimator.ofFloat(imageView, "alpha", 1.0f, 0.8f, 1.0f)
                
                val celebrationSet = AnimatorSet()
                celebrationSet.playTogether(celebrationScaleX, celebrationScaleY, celebrationAlpha)
                celebrationSet.duration = 600
                celebrationSet.interpolator = android.view.animation.BounceInterpolator()
                celebrationSet.start()
            }, index * 100L) // Stagger the animations
        }
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
        Handler(Looper.getMainLooper()).postDelayed({
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
