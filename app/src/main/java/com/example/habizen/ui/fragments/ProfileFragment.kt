package com.example.habizen.ui.fragments

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.example.habizen.R
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.habizen.data.DailyStat
import com.example.habizen.data.MoodEntry
import com.example.habizen.databinding.FragmentProfileBinding
import com.example.habizen.ui.adapters.AchievementAdapter
import com.example.habizen.ui.auth.LoginActivity
import com.example.habizen.utils.AnalyticsManager
import com.example.habizen.utils.AchievementsManager
import com.example.habizen.utils.PreferencesManager
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import kotlin.math.roundToInt
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ProfileFragment : Fragment() {
    
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configureCharts()
        setupAchievementsSection()
        setupLogoutButton()
        populateUserProfile()
        renderAnalytics()
    }

    override fun onResume() {
        super.onResume()
        populateUserProfile()
        renderAnalytics()
    }

    private fun populateUserProfile() {
        val context = requireContext()
        val user = PreferencesManager.getUser(context)
        val dateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

        if (user != null) {
            binding.tvUserName.text = user.name.ifEmpty { "Habizen Explorer" }
            binding.tvUserEmail.text = user.email
            binding.tvUserEmail.isVisible = user.email.isNotEmpty()
            binding.tvJoinDate.text = "Joined ${dateFormatter.format(Date(user.joinDate))}"
        } else {
            binding.tvUserName.text = "Welcome to Habizen"
            binding.tvUserEmail.isVisible = false
            binding.tvJoinDate.text = "Let's start building healthy habits"
        }
    }

    private fun renderAnalytics() {
        val context = requireContext()

        val habitSummary = AnalyticsManager.getHabitSummary(context)
        binding.tvTotalHabits.text = "${habitSummary.totalHabits}"
        binding.tvHabitCompletion.text = "${habitSummary.completionRate.roundToInt()}%"

        val moodSummary = AnalyticsManager.getMoodSummary(context)
        if (moodSummary.distribution.isEmpty()) {
            binding.tvMoodAverage.text = "No Data"
            binding.tvMoodHint.text = "Start tracking"
        } else {
            binding.tvMoodAverage.text = "${String.format(Locale.getDefault(), "%.1f", moodSummary.averageScore)}/5"
            binding.tvMoodHint.text = "Keep tracking"
        }

        val hydrationSummary = AnalyticsManager.getHydrationSummary(context)
        binding.tvHydrationToday.text = "${hydrationSummary.todayTotal} ml"

        binding.tvLongestStreak.text = "${habitSummary.longestStreak} days"

        val habitTrend = AnalyticsManager.getHabitWeeklyCompletion(context)
        renderHabitChart(habitTrend)

        renderMoodChart(moodSummary.distribution)

        renderHydrationChart(hydrationSummary.weeklyTotals, hydrationSummary.dailyGoal)

        val achievements = AchievementsManager.getAchievements(context)
        binding.rvAchievements.adapter = AchievementAdapter(achievements)
        binding.tvAchievementsEmpty.isVisible = achievements.none { it.isUnlocked }
    }

    private fun setupLogoutButton() {
        binding.btnLogout.setOnClickListener {
            showLogoutConfirmationDialog()
        }
    }

    private fun showLogoutConfirmationDialog() {
        androidx.appcompat.app.AlertDialog.Builder(requireContext(), R.style.CustomLogoutDialog)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Logout") { _, _ ->
                performLogout()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun performLogout() {
        // Clear user preferences
        PreferencesManager.clearAllData(requireContext())

        // Navigate to login screen
        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }

    private fun setupAchievementsSection() {
        binding.rvAchievements.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun configureCharts() {
        binding.chartHabits.apply {
            setNoDataText("Track habits to see your weekly progress")
            description.isEnabled = false
            legend.isEnabled = false
            axisRight.isEnabled = false
            axisLeft.apply {
                axisMinimum = 0f
                axisMaximum = 100f
                textColor = Color.GRAY
                gridColor = Color.parseColor("#22FFFFFF")
            }
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                granularity = 1f
                textColor = Color.GRAY
                setDrawGridLines(false)
            }
        }

        binding.chartMood.apply {
            setNoDataText("Log moods to view your emotional balance")
            description.isEnabled = false
            setUsePercentValues(true)
            legend.isEnabled = false
            setEntryLabelColor(Color.WHITE)
            setEntryLabelTextSize(12f)
        }

        binding.chartHydration.apply {
            setNoDataText("Add hydration entries to see your trend")
            description.isEnabled = false
            legend.isEnabled = false
            axisRight.isEnabled = false
            axisLeft.apply {
                axisMinimum = 0f
                textColor = Color.GRAY
                gridColor = Color.parseColor("#22FFFFFF")
            }
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                granularity = 1f
                textColor = Color.GRAY
                setDrawGridLines(false)
            }
        }
    }

    private fun renderHabitChart(stats: List<DailyStat>) {
        if (stats.isEmpty()) {
            binding.chartHabits.clear()
            binding.chartHabits.invalidate()
            return
        }

        val entries = stats.mapIndexed { index, stat ->
            BarEntry(index.toFloat(), stat.value)
        }
        val labels = stats.map { it.label }

        val dataSet = BarDataSet(entries, "Habit Completion").apply {
            color = ColorTemplate.rgb("#42A5F5") // primary
            valueTextColor = Color.WHITE
            valueTextSize = 12f
        }

        binding.chartHabits.apply {
            xAxis.valueFormatter = IndexAxisValueFormatter(labels)
            data = BarData(dataSet).apply {
                barWidth = 0.4f
            }
            animateY(800)
            invalidate()
        }
    }

    private fun renderMoodChart(distribution: Map<String, Int>) {
        if (distribution.isEmpty()) {
            binding.chartMood.clear()
            binding.chartMood.invalidate()
            return
        }

        val entries = distribution.entries.map { (emoji, count) ->
            PieEntry(count.toFloat(), MoodEntry.MOOD_NAMES[emoji] ?: emoji)
        }

        val colors = listOf(
            ColorTemplate.rgb("#42A5F5"), // primary
            ColorTemplate.rgb("#F06292"), // secondary
            ColorTemplate.rgb("#66BB6A"), // success
            ColorTemplate.rgb("#FFA726"), // warning
            ColorTemplate.rgb("#FF5252")  // error
        )

        val dataSet = PieDataSet(entries, "Mood Distribution").apply {
            sliceSpace = 2f
            setColors(colors)
            valueTextColor = Color.WHITE
            valueTextSize = 12f
        }

        binding.chartMood.apply {
            data = PieData(dataSet)
            animateY(800)
            invalidate()
        }
    }

    private fun renderHydrationChart(stats: List<DailyStat>, goal: Int) {
        if (stats.isEmpty()) {
            binding.chartHydration.clear()
            binding.chartHydration.invalidate()
            return
        }

        val entries = stats.mapIndexed { index, stat ->
            Entry(index.toFloat(), stat.value)
        }
        val labels = stats.map { it.label }

        val dataSet = LineDataSet(entries, "Hydration").apply {
            color = ColorTemplate.rgb("#F06292") // secondary
            circleRadius = 4f
            setCircleColor(ColorTemplate.rgb("#F06292"))
            lineWidth = 2.4f
            valueTextColor = Color.WHITE
            valueTextSize = 11f
            setDrawFilled(true)
            fillColor = ColorTemplate.rgb("#EC407A") // secondary_variant
        }

        binding.chartHydration.apply {
            xAxis.valueFormatter = IndexAxisValueFormatter(labels)
            axisLeft.axisMaximum = (listOf(stats.maxOf { it.value }, goal.toFloat()).maxOrNull() ?: 0f) * 1.2f
            data = LineData(dataSet)
            animateX(800)
            invalidate()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
