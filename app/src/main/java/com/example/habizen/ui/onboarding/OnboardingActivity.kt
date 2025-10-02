package com.example.habizen.ui.onboarding

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.example.habizen.R
import com.example.habizen.databinding.ActivityOnboardingBinding
import com.example.habizen.ui.auth.LoginActivity
import com.example.habizen.utils.PreferencesManager
import com.example.habizen.utils.ThemeUtils
import com.example.habizen.data.OnboardingPage

class OnboardingActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityOnboardingBinding
    private lateinit var adapter: OnboardingAdapter
    private var currentPage = 0
    
    // Onboarding pages with custom images and relevant content
    private val pages by lazy {
        listOf(
            OnboardingPage(
                title = getString(R.string.onboarding_title_1),
                description = getString(R.string.onboarding_desc_1),
                illustrationRes = R.drawable.onb1
            ),
            OnboardingPage(
                title = getString(R.string.onboarding_title_2),
                description = getString(R.string.onboarding_desc_2),
                illustrationRes = R.drawable.onb2
            )
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeUtils.applyUserTheme(this)
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupViewPager()
        setupIndicators()
        setupClickListeners()
    }
    
    private fun setupViewPager() {
        adapter = OnboardingAdapter(pages)
        binding.viewPager.adapter = adapter
        
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                currentPage = position
                updateIndicators()
                updateButtons()
            }
        })
    }
    
    private fun setupIndicators() {
        val indicators = arrayOfNulls<View>(pages.size)
        
        for (i in pages.indices) {
            indicators[i] = View(this).apply {
                val indicatorLayoutParams = android.widget.LinearLayout.LayoutParams(
                    16, 16
                )
                layoutParams = indicatorLayoutParams
                setBackgroundResource(R.drawable.indicator_inactive)
            }
            binding.indicatorLayout.addView(indicators[i])
            
            // Add margin except for the last indicator
            if (i < pages.size - 1) {
                val params = indicators[i]?.layoutParams as? android.widget.LinearLayout.LayoutParams
                params?.rightMargin = 16
            }
        }
        
        updateIndicators()
    }
    
    private fun updateIndicators() {
        for (i in 0 until binding.indicatorLayout.childCount) {
            val indicator = binding.indicatorLayout.getChildAt(i)
            if (i == currentPage) {
                indicator.setBackgroundResource(R.drawable.indicator_active)
            } else {
                indicator.setBackgroundResource(R.drawable.indicator_inactive)
            }
        }
    }
    
    private fun updateButtons() {
        if (currentPage == pages.size - 1) {
            binding.btnNext.visibility = View.GONE
            binding.btnGetStarted.visibility = View.VISIBLE
        } else {
            binding.btnNext.visibility = View.VISIBLE
            binding.btnGetStarted.visibility = View.GONE
        }
    }
    
    private fun setupClickListeners() {
        binding.tvSkip.setOnClickListener {
            finishOnboarding()
        }
        
        binding.btnNext.setOnClickListener {
            if (currentPage < pages.size - 1) {
                binding.viewPager.currentItem = currentPage + 1
            }
        }
        
        binding.btnGetStarted.setOnClickListener {
            finishOnboarding()
        }
    }
    
    private fun finishOnboarding() {
        PreferencesManager.setOnboardingCompleted(this, true)
        
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
        
        // Add transition animation
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
    }
}
