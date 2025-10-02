package com.example.habizen.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import androidx.appcompat.app.AppCompatActivity
import com.example.habizen.databinding.ActivityLoginBinding
import com.example.habizen.ui.main.MainActivity
import com.example.habizen.utils.PreferencesManager
import com.example.habizen.utils.ThemeUtils
import com.example.habizen.data.User
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import java.security.MessageDigest

class LoginActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeUtils.applyUserTheme(this)
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupClickListeners()
        loadSavedCredentials()
    }
    
    private fun setupClickListeners() {
        binding.btnLogin.setOnClickListener {
            handleLogin()
        }
        
        binding.tvSignUp.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }
        
        binding.tvForgotPassword.setOnClickListener {
            showForgotPasswordDialog()
        }
    }
    
    private fun loadSavedCredentials() {
        if (binding.cbRememberMe.isChecked) {
            val user = PreferencesManager.getUser(this)
            if (user != null) {
                binding.etEmail.setText(user.email)
            }
        }
    }
    
    private fun handleLogin() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString()
        
        if (validateInput(email, password)) {
            val user = PreferencesManager.getUser(this)
            
            if (user != null && user.email == email && user.password == hashPassword(password)) {
                // Login successful
                PreferencesManager.setLoggedIn(this, true)
                
                if (binding.cbRememberMe.isChecked) {
                    // Save credentials for next time
                    PreferencesManager.saveUser(this, user)
                }
                
                showSuccessMessage("Welcome back, ${user.name}!")
                
                // Navigate to main activity
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
                
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
            } else {
                showError("Invalid email or password")
            }
        }
    }
    
    private fun validateInput(email: String, password: String): Boolean {
        var isValid = true
        
        // Email validation
        if (email.isEmpty()) {
            binding.tilEmail.error = "Email is required"
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = "Enter a valid email"
            isValid = false
        } else {
            binding.tilEmail.error = null
        }
        
        // Password validation
        if (password.isEmpty()) {
            binding.tilPassword.error = "Password is required"
            isValid = false
        } else if (password.length < 6) {
            binding.tilPassword.error = "Password must be at least 6 characters"
            isValid = false
        } else {
            binding.tilPassword.error = null
        }
        
        return isValid
    }
    
    private fun showForgotPasswordDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Forgot Password")
            .setMessage("Please contact support to reset your password. This is a demo app with local storage.")
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
    
    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
            .setBackgroundTint(getColor(android.R.color.holo_red_light))
            .show()
    }
    
    private fun showSuccessMessage(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT)
            .setBackgroundTint(getColor(android.R.color.holo_green_light))
            .show()
    }
    
    private fun hashPassword(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(password.toByteArray())
        return hash.fold("", { str, it -> str + "%02x".format(it) })
    }
}
