package com.example.habizen.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import androidx.appcompat.app.AppCompatActivity
import com.example.habizen.databinding.ActivitySignUpBinding
import com.example.habizen.data.User
import com.example.habizen.utils.PreferencesManager
import com.example.habizen.utils.ThemeUtils
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import java.security.MessageDigest

class SignUpActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivitySignUpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeUtils.applyUserTheme(this)
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupClickListeners()
    }
    
    private fun setupClickListeners() {
        binding.btnSignUp.setOnClickListener {
            handleSignUp()
        }
        
        binding.tvLogin.setOnClickListener {
            finish()
        }
    }
    
    private fun handleSignUp() {
        val fullName = binding.etFullName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString()
        val confirmPassword = binding.etConfirmPassword.text.toString()
        val termsAccepted = binding.cbTerms.isChecked
        
        if (validateInput(fullName, email, password, confirmPassword, termsAccepted)) {
            // Check if user already exists
            val existingUser = PreferencesManager.getUser(this)
            if (existingUser != null && existingUser.email == email) {
                showError("An account with this email already exists")
                return
            }
            
            // Create new user
            val user = User(
                name = fullName,
                email = email,
                password = hashPassword(password)
            )
            
            // Save user
            PreferencesManager.saveUser(this, user)
            PreferencesManager.setLoggedIn(this, true)
            
            showSuccessDialog(fullName)
        }
    }
    
    private fun validateInput(
        fullName: String, 
        email: String, 
        password: String, 
        confirmPassword: String,
        termsAccepted: Boolean
    ): Boolean {
        var isValid = true
        
        // Full name validation
        if (fullName.isEmpty()) {
            binding.tilFullName.error = "Full name is required"
            isValid = false
        } else if (fullName.length < 2) {
            binding.tilFullName.error = "Name must be at least 2 characters"
            isValid = false
        } else {
            binding.tilFullName.error = null
        }
        
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
        
        // Confirm password validation
        if (confirmPassword.isEmpty()) {
            binding.tilConfirmPassword.error = "Please confirm your password"
            isValid = false
        } else if (password != confirmPassword) {
            binding.tilConfirmPassword.error = "Passwords do not match"
            isValid = false
        } else {
            binding.tilConfirmPassword.error = null
        }
        
        // Terms validation
        if (!termsAccepted) {
            showError("Please accept the Terms & Conditions")
            isValid = false
        }
        
        return isValid
    }
    
    private fun showSuccessDialog(userName: String) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Welcome to WellNest!")
            .setMessage("Your account has been created successfully, $userName. You can now start tracking your wellness journey.")
            .setPositiveButton("Get Started") { dialog, _ ->
                dialog.dismiss()
                navigateToMain()
            }
            .setCancelable(false)
            .show()
    }
    
    private fun navigateToMain() {
        val intent = Intent(this, com.example.habizen.ui.main.MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
    }
    
    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
            .setBackgroundTint(getColor(android.R.color.holo_red_light))
            .show()
    }
    
    private fun hashPassword(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(password.toByteArray())
        return hash.fold("", { str, it -> str + "%02x".format(it) })
    }
}
