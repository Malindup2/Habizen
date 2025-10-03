package com.example.habizen.ui.onboarding

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.habizen.databinding.ActivityOnboarding2Binding
import com.example.habizen.utils.ThemeUtils

class Onboarding2Activity : AppCompatActivity() {

    private lateinit var binding: ActivityOnboarding2Binding

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeUtils.applyUserTheme(this)
        super.onCreate(savedInstanceState)
        binding = ActivityOnboarding2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnGetStarted.setOnClickListener {
            // Finish onboarding and go to login
            finishOnboarding()
        }

        binding.tvSkip.setOnClickListener {
            // Skip to login
            finishOnboarding()
        }
    }

    private fun finishOnboarding() {
        com.example.habizen.utils.PreferencesManager.setOnboardingCompleted(this, true)

        val intent = Intent(this, com.example.habizen.ui.auth.LoginActivity::class.java)
        startActivity(intent)
        finish()

        // Add transition animation
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
    }
}