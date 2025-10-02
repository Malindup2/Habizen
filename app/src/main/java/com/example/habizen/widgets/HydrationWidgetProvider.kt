package com.example.habizen.widgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.habizen.R
import com.example.habizen.ui.main.MainActivity
import com.example.habizen.utils.PreferencesManager
import com.example.habizen.workers.HydrationQuickAddReceiver
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HydrationWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        appWidgetIds.forEach { id ->
            val views = buildRemoteViews(context)
            appWidgetManager.updateAppWidget(id, views)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_REFRESH) {
            updateAllWidgets(context)
        }
    }

    companion object {
        private const val ACTION_REFRESH = "com.example.habizen.widget.ACTION_REFRESH"

        fun updateAllWidgets(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(ComponentName(context, HydrationWidgetProvider::class.java))
            ids.forEach { id ->
                manager.updateAppWidget(id, buildRemoteViews(context))
            }
        }

        private fun buildRemoteViews(context: Context): RemoteViews {
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val entries = PreferencesManager.getHydrationEntries(context)
            val todayTotal = entries.filter { it.date == today }.sumOf { it.amount }
            val goal = PreferencesManager.getHydrationGoal(context)

            val views = RemoteViews(context.packageName, R.layout.widget_hydration)
            views.setTextViewText(R.id.tvWidgetProgress, "${todayTotal} / ${goal} ml")

            val quickAddIntent = Intent(context, HydrationQuickAddReceiver::class.java).apply {
                putExtra("amount", 250)
            }
            val quickAddPendingIntent = PendingIntent.getBroadcast(
                context,
                250,
                quickAddIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.btnWidgetQuickAdd, quickAddPendingIntent)

            val openAppIntent = Intent(context, MainActivity::class.java).apply {
                putExtra("fragment", "hydration")
            }
            val openAppPendingIntent = PendingIntent.getActivity(
                context,
                0,
                openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.tvWidgetTitle, openAppPendingIntent)
            views.setOnClickPendingIntent(R.id.tvWidgetProgress, openAppPendingIntent)

            return views
        }

        fun requestRefresh(context: Context) {
            context.sendBroadcast(Intent(context, HydrationWidgetProvider::class.java).apply {
                action = ACTION_REFRESH
            })
        }
    }
}
