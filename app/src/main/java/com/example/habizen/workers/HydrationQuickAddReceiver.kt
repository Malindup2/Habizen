package com.example.habizen.workers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.example.habizen.data.HydrationData
import com.example.habizen.utils.PreferencesManager
import com.example.habizen.widgets.HydrationWidgetProvider
import java.text.SimpleDateFormat
import java.util.*

class HydrationQuickAddReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return
        
        val amount = intent.getIntExtra("amount", 0)
        if (amount <= 0) return
        
        val currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        
        val hydrationEntry = HydrationData(
            id = System.currentTimeMillis().toString(),
            amount = amount,
            time = currentTime,
            date = currentDate
        )

        PreferencesManager.addHydrationEntry(context, hydrationEntry)
        HydrationWidgetProvider.requestRefresh(context)
        HydrationReminderWorker.schedule(context)
        
        Toast.makeText(
            context,
            "Added ${amount}ml to your hydration log! 💧",
            Toast.LENGTH_SHORT
        ).show()
    }
}
