package com.example.habizen.ui.profile

import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.habizen.data.User
import com.example.habizen.databinding.BottomsheetEditProfileBinding
import com.example.habizen.utils.PreferencesManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import java.security.MessageDigest

class EditProfileBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomsheetEditProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomsheetEditProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        populateFields()
        setupListeners()
    }

    private fun populateFields() {
        val user = PreferencesManager.getUser(requireContext())
        if (user != null) {
            binding.etName.setText(user.name)
            binding.etEmail.setText(user.email)
        }
    }

    private fun setupListeners() {
        binding.btnCancel.setOnClickListener { dismiss() }
        binding.btnSave.setOnClickListener { saveProfile() }
    }

    private fun saveProfile() {
        val name = binding.etName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val newPassword = binding.etPassword.text.toString()

        if (name.isEmpty()) {
            binding.tilName.error = "Name required"
            return
        } else {
            binding.tilName.error = null
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = "Invalid email"
            return
        } else {
            binding.tilEmail.error = null
        }

        val existing = PreferencesManager.getUser(requireContext())
        val passwordHash = when {
            newPassword.isEmpty() && existing != null -> existing.password
            newPassword.length >= 6 -> hashPassword(newPassword)
            newPassword.isNotEmpty() -> {
                binding.tilPassword.error = "Password must be 6+ characters"
                return
            }
            else -> hashPassword("habizen")
        }

        val updatedUser = User(
            name = name,
            email = email,
            password = passwordHash,
            joinDate = existing?.joinDate ?: System.currentTimeMillis(),
            profileImagePath = existing?.profileImagePath ?: ""
        )

        PreferencesManager.saveUser(requireContext(), updatedUser)
        Snackbar.make(binding.root, "Profile updated", Snackbar.LENGTH_SHORT).show()
        dismiss()
    }

    private fun hashPassword(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(password.toByteArray())
        return hash.joinToString(separator = "") { byte -> "%02x".format(byte) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
