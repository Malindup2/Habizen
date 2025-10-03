package com.example.habizen

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.habizen.databinding.ActivityMainBinding
import com.example.habizen.ui.auth.LoginActivity
import com.example.habizen.ui.fragments.HabitsFragment
import com.example.habizen.ui.fragments.HydrationFragment
import com.example.habizen.ui.fragments.MoodFragment
import com.example.habizen.ui.fragments.ProfileFragment
import com.example.habizen.utils.GoalCompletionNotificationManager
import com.example.habizen.utils.PreferencesManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private var currentFragment: Fragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Initialize notification manager for goal completion notifications
        GoalCompletionNotificationManager.initialize(this)
        
        // Request notification permission if needed (Android 13+)
        if (GoalCompletionNotificationManager.shouldRequestNotificationPermission(this)) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                1001
            )
        }
        
        setSupportActionBar(binding.toolbar)
        
        setupBottomNavigation()
        handleIntent()
        
        // Default to habits fragment
        if (savedInstanceState == null) {
            showFragment(HabitsFragment(), "Habits")
        }
    }
    
    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_habits -> {
                    showFragment(HabitsFragment(), "Habits")
                    binding.fabAddHabit.show()
                    true
                }
                R.id.nav_mood -> {
                    showFragment(MoodFragment(), "Mood Journal")
                    binding.fabAddHabit.hide()
                    true
                }
                R.id.nav_hydration -> {
                    showFragment(HydrationFragment(), "Hydration")
                    binding.fabAddHabit.hide()
                    true
                }
                R.id.nav_profile -> {
                    showFragment(ProfileFragment(), "Profile")
                    binding.fabAddHabit.hide()
                    true
                }
                else -> false
            }
        }
        
        // Set default selection
        binding.bottomNavigation.selectedItemId = R.id.nav_habits
    }
    
    private fun handleIntent() {
        val fragmentName = intent.getStringExtra("fragment")
        when (fragmentName) {
            "hydration" -> {
                binding.bottomNavigation.selectedItemId = R.id.nav_hydration
            }
        }
    }
    
    private fun showFragment(fragment: Fragment, title: String) {
        currentFragment = fragment
        binding.toolbar.title = title
        
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_settings -> {
                // Handle settings
                true
            }
            R.id.menu_share -> {
                shareApp()
                true
            }
            R.id.menu_logout -> {
                showLogoutDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    private fun shareApp() {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, 
                "Check out Habizen - Your complete wellness tracking app! Download it today and start your health journey.")
        }
        startActivity(Intent.createChooser(shareIntent, "Share Habizen"))
    }
    
    private fun showLogoutDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout from Habizen?")
            .setPositiveButton("Logout") { _, _ ->
                logout()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun logout() {
        PreferencesManager.setLoggedIn(this, false)
        
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
