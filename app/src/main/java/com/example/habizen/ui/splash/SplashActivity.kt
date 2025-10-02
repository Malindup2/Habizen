package com.example.habizen.ui.splash

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.habizen.databinding.ActivitySplashBinding
import com.example.habizen.ui.onboarding.OnboardingActivity
import com.example.habizen.ui.auth.LoginActivity
import com.example.habizen.ui.main.MainActivity
import com.example.habizen.utils.PreferencesManager
import com.example.habizen.utils.ThemeUtils

class SplashActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivitySplashBinding
    private val splashDelay = 3000L

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeUtils.applyUserTheme(this)
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupAnimations()
        navigateAfterDelay()
    }
    
    private fun setupAnimations() {
        // Logo scale and fade-in animation
        val logoScaleX = ObjectAnimator.ofFloat(binding.ivAppLogo, "scaleX", 0f, 1f)
        val logoScaleY = ObjectAnimator.ofFloat(binding.ivAppLogo, "scaleY", 0f, 1f)
        val logoAlpha = ObjectAnimator.ofFloat(binding.ivAppLogo, "alpha", 0f, 1f)
        
        // App name fade-in animation
        val nameAlpha = ObjectAnimator.ofFloat(binding.tvAppName, "alpha", 0f, 1f)
        val nameTranslation = ObjectAnimator.ofFloat(binding.tvAppName, "translationY", 50f, 0f)
        
        // Tagline fade-in animation
        val taglineAlpha = ObjectAnimator.ofFloat(binding.tvAppTagline, "alpha", 0f, 1f)
        val taglineTranslation = ObjectAnimator.ofFloat(binding.tvAppTagline, "translationY", 30f, 0f)
        
        // Version fade-in animation
        val versionAlpha = ObjectAnimator.ofFloat(binding.tvVersion, "alpha", 0f, 1f)
        
        // Combine animations
        val logoAnimatorSet = AnimatorSet().apply {
            playTogether(logoScaleX, logoScaleY, logoAlpha)
            duration = 800
        }
        
        val textAnimatorSet = AnimatorSet().apply {
            playTogether(nameAlpha, nameTranslation)
            duration = 600
            startDelay = 400
        }
        
        val taglineAnimatorSet = AnimatorSet().apply {
            playTogether(taglineAlpha, taglineTranslation)
            duration = 600
            startDelay = 600
        }
        
        val versionAnimatorSet = AnimatorSet().apply {
            play(versionAlpha)
            duration = 400
            startDelay = 800
        }
        
        // Start animations
        logoAnimatorSet.start()
        textAnimatorSet.start()
        taglineAnimatorSet.start()
        versionAnimatorSet.start()
    }
    
    private fun navigateAfterDelay() {
        Handler(Looper.getMainLooper()).postDelayed({
            navigateToAppropriateScreen()
        }, splashDelay)
    }
    
    private fun navigateToAppropriateScreen() {
        val intent = when {
            PreferencesManager.isFirstLaunch(this) -> {
                PreferencesManager.setFirstLaunch(this, false)
                Intent(this, OnboardingActivity::class.java)
            }
            !PreferencesManager.isOnboardingCompleted(this) -> {
                Intent(this, OnboardingActivity::class.java)
            }
            !PreferencesManager.isLoggedIn(this) -> {
                Intent(this, LoginActivity::class.java)
            }
            else -> {
                Intent(this, MainActivity::class.java)
            }
        }
        
        startActivity(intent)
        finish()
        
        // Add transition animation
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
}
