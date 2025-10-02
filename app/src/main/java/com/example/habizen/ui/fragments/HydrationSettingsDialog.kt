package com.example.habizen.ui.fragments

import android.app.Dialog
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.habizen.R
import com.example.habizen.databinding.DialogHydrationSettingsBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class HydrationSettingsDialog(
    private val currentGoal: Int,
    private val onGoalSet: (Int) -> Unit
) : DialogFragment() {

    private var _binding: DialogHydrationSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogHydrationSettingsBinding.inflate(layoutInflater)
        
        setupUI()
        
        return MaterialAlertDialogBuilder(requireContext(), R.style.CustomHydrationSettingsDialog)
            .setView(binding.root)
            .setTitle("Hydration Settings")
            .setPositiveButton("Save") { _, _ ->
                saveSettings()
            }
            .setNegativeButton("Cancel", null)
            .create()
    }

    private fun setupUI() {
        binding.apply {
            etGoal.setText(currentGoal.toString())
            
            // Quick select buttons
            btn1500ml.setOnClickListener { etGoal.setText("1500") }
            btn2000ml.setOnClickListener { etGoal.setText("2000") }
            btn2500ml.setOnClickListener { etGoal.setText("2500") }
            btn3000ml.setOnClickListener { etGoal.setText("3000") }
        }
    }

    private fun saveSettings() {
        val goalText = binding.etGoal.text.toString()
        val goal = goalText.toIntOrNull()
        
        if (goal != null && goal >= 500 && goal <= 5000) {
            onGoalSet(goal)
        } else {
            Toast.makeText(
                requireContext(),
                "Please enter a valid goal between 500ml and 5000ml",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
