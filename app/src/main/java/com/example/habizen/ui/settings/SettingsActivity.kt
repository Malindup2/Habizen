package com.example.habizen.ui.settings

import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.habizen.databinding.ActivitySettingsBinding
import com.example.habizen.ui.auth.LoginActivity
import com.example.habizen.ui.profile.EditProfileBottomSheet
import com.example.habizen.utils.DataExporter
import com.example.habizen.utils.PreferencesManager
import com.example.habizen.utils.ThemeUtils
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import java.util.Locale

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeUtils.applyUserTheme(this)
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupThemeToggle()
        setupHydrationControls()
        setupExportActions()
        setupAccountActions()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }

    private fun setupThemeToggle() {
        val mode = PreferencesManager.getThemeMode(this)
        val group = binding.themeToggleGroup
        when (mode) {
            "light" -> group.check(binding.btnThemeLight.id)
            "dark" -> group.check(binding.btnThemeDark.id)
            else -> group.check(binding.btnThemeSystem.id)
        }

        group.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            val selectedMode = when (checkedId) {
                binding.btnThemeLight.id -> "light"
                binding.btnThemeDark.id -> "dark"
                else -> "system"
            }
            PreferencesManager.setThemeMode(this, selectedMode)
            ThemeUtils.applyTheme(selectedMode)
        }
    }

    private fun setupHydrationControls() {
        var settings = PreferencesManager.getHydrationSettings(this)
        binding.switchHydrationNotifications.isChecked = settings.notificationsEnabled
        binding.sliderReminderInterval.value = settings.reminderInterval.toFloat()
        binding.tvReminderInterval.text = getString(
            com.example.habizen.R.string.reminder_interval_template,
            settings.reminderInterval
        )
        updateReminderWindowText(settings.startTime, settings.endTime)

        binding.switchHydrationNotifications.setOnCheckedChangeListener { _, isChecked ->
            settings = settings.copy(notificationsEnabled = isChecked)
            PreferencesManager.saveHydrationSettings(this, settings)
            com.example.habizen.workers.HydrationReminderWorker.schedule(this)
        }

        binding.sliderReminderInterval.addOnChangeListener { _, value, fromUser ->
            if (fromUser) {
                val minutes = value.toInt()
                binding.tvReminderInterval.text = getString(
                    com.example.habizen.R.string.reminder_interval_template,
                    minutes
                )
                settings = settings.copy(reminderInterval = minutes)
                PreferencesManager.saveHydrationSettings(this, settings)
                com.example.habizen.workers.HydrationReminderWorker.schedule(this)
            }
        }

        binding.btnEditHydrationWindow.setOnClickListener {
            pickTime(settings.startTime) { start ->
                pickTime(settings.endTime) { end ->
                    settings = settings.copy(startTime = start, endTime = end)
                    PreferencesManager.saveHydrationSettings(this, settings)
                    updateReminderWindowText(start, end)
                    com.example.habizen.workers.HydrationReminderWorker.schedule(this)
                }
            }
        }
    }

    private fun setupExportActions() {
        binding.btnExportJson.setOnClickListener {
            exportData { uri ->
                shareFile(uri, "application/json")
            }
        }

        binding.btnExportCsv.setOnClickListener {
            exportCsv { uri ->
                shareFile(uri, "text/csv")
            }
        }
    }

    private fun setupAccountActions() {
        binding.btnEditProfile.setOnClickListener {
            EditProfileBottomSheet().show(supportFragmentManager, "EditProfile")
        }

        binding.btnDeleteAccount.setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle("Delete account")
                .setMessage("This will remove all your data from this device. This action cannot be undone.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Delete") { _, _ ->
                    PreferencesManager.clearAllData(this)
                    PreferencesManager.setLoggedIn(this, false)
                    Snackbar.make(binding.root, "Account deleted", Snackbar.LENGTH_SHORT).show()
                    val intent = Intent(this, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
                .show()
        }
    }

    private fun exportData(onReady: (Uri) -> Unit) {
        val result = DataExporter.exportJson(this)
        if (result != null) {
            onReady(result)
        } else {
            Snackbar.make(binding.root, "Nothing to export yet", Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun exportCsv(onReady: (Uri) -> Unit) {
        val result = DataExporter.exportCsv(this)
        if (result != null) {
            onReady(result)
        } else {
            Snackbar.make(binding.root, "Nothing to export yet", Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun shareFile(uri: Uri, type: String) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            setType(type)
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(shareIntent, "Share export"))
    }

    private fun pickTime(initial: String, onSelected: (String) -> Unit) {
        val parts = initial.split(":")
        val hour = parts.getOrNull(0)?.toIntOrNull() ?: 8
        val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0
        TimePickerDialog(this, { _, h, m ->
            onSelected(String.format(Locale.getDefault(), "%02d:%02d", h, m))
        }, hour, minute, true).show()
    }

    private fun updateReminderWindowText(start: String, end: String) {
        binding.tvReminderWindow.text = getString(
            com.example.habizen.R.string.reminder_window_template,
            start,
            end
        )
    }
}
