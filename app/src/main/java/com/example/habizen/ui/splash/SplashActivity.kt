package com.example.habizen.ui.splash

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat
import com.example.habizen.R
import com.example.habizen.databinding.ActivitySplashBinding
import com.example.habizen.ui.onboarding.Onboarding1Activity
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
        
        // Set colored app name
        binding.tvAppName.text = HtmlCompat.fromHtml(getString(R.string.app_name_colored), HtmlCompat.FROM_HTML_MODE_LEGACY)
        
        setupAnimations()
        navigateAfterDelay()
    }
    
    private fun setupAnimations() {
        // App name fade-in animation
        val nameAlpha = ObjectAnimator.ofFloat(binding.tvAppName, "alpha", 0f, 1f)
        val nameTranslation = ObjectAnimator.ofFloat(binding.tvAppName, "translationY", 50f, 0f)

        // Tagline fade-in animation
        val taglineAlpha = ObjectAnimator.ofFloat(binding.tvAppTagline, "alpha", 0f, 1f)
        val taglineTranslation = ObjectAnimator.ofFloat(binding.tvAppTagline, "translationY", 30f, 0f)

        // Combine animations
        val textAnimatorSet = AnimatorSet().apply {
            playTogether(nameAlpha, nameTranslation)
            duration = 600
        }

        val taglineAnimatorSet = AnimatorSet().apply {
            playTogether(taglineAlpha, taglineTranslation)
            duration = 600
            startDelay = 200
        }

        // Start animations
        textAnimatorSet.start()
        taglineAnimatorSet.start()
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
                Intent(this, Onboarding1Activity::class.java)
            }
            !PreferencesManager.isOnboardingCompleted(this) -> {
                Intent(this, Onboarding1Activity::class.java)
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
