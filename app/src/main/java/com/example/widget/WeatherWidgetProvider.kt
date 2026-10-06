package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.data.local.AppDatabase
import com.example.data.network.WeatherNetwork
import com.example.data.repository.WeatherRepository
import com.example.ui.model.WeatherCodeMapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale

class WeatherWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_REFRESH_WIDGET) {
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = AppDatabase.getInstance(context)
                    val repo = WeatherRepository(
                        db.weatherDao(),
                        WeatherNetwork.api,
                        WeatherNetwork.geocodingApi,
                        context.applicationContext
                    )
                    repo.syncWidgetDataNow()
                    updateAllWidgets(context)
                } catch (e: Exception) {
                    Log.e("WeatherWidgetProvider", "Refresh error: ${e.message}")
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }

    companion object {
        const val ACTION_REFRESH_WIDGET = "com.example.weatherrain.ACTION_REFRESH_WIDGET"

        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val thisWidget = ComponentName(context, WeatherWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)
            for (appWidgetId in appWidgetIds) {
                updateWidget(context, appWidgetManager, appWidgetId)
            }
        }

        fun canPinWidget(context: Context): Boolean {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val appWidgetManager = context.getSystemService(AppWidgetManager::class.java)
                appWidgetManager?.isRequestPinAppWidgetSupported == true
            } else {
                false
            }
        }

        fun pinWidgetToHomeScreen(context: Context): Boolean {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val appWidgetManager = context.getSystemService(AppWidgetManager::class.java)
                if (appWidgetManager != null && appWidgetManager.isRequestPinAppWidgetSupported) {
                    val myProvider = ComponentName(context, WeatherWidgetProvider::class.java)
                    return appWidgetManager.requestPinAppWidget(myProvider, null, null)
                }
            }
            return false
        }

        private fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.widget_weather_7day)

            // Setup Launch App Intent
            val launchIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingLaunch = PendingIntent.getActivity(
                context,
                0,
                launchIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root, pendingLaunch)
            views.setOnClickPendingIntent(R.id.widget_location_text, pendingLaunch)

            // Setup Refresh Intent
            val refreshIntent = Intent(context, WeatherWidgetProvider::class.java).apply {
                action = ACTION_REFRESH_WIDGET
            }
            val pendingRefresh = PendingIntent.getBroadcast(
                context,
                1001,
                refreshIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_refresh_button, pendingRefresh)

            // Immediately apply initial layout to avoid launcher timeout
            try {
                appWidgetManager.updateAppWidget(appWidgetId, views)
            } catch (e: Exception) {
                Log.w("WeatherWidgetProvider", "Initial sync update error: ${e.message}")
            }

            // 7 days definition
            data class DayWidgetViews(
                val nameId: Int,
                val iconId: Int,
                val tempMaxId: Int,
                val tempMinId: Int,
                val rainAmountId: Int
            )

            val dayContainers = listOf(
                DayWidgetViews(R.id.day_0_name, R.id.day_0_icon, R.id.day_0_temp_max, R.id.day_0_temp_min, R.id.day_0_rain_amount),
                DayWidgetViews(R.id.day_1_name, R.id.day_1_icon, R.id.day_1_temp_max, R.id.day_1_temp_min, R.id.day_1_rain_amount),
                DayWidgetViews(R.id.day_2_name, R.id.day_2_icon, R.id.day_2_temp_max, R.id.day_2_temp_min, R.id.day_2_rain_amount),
                DayWidgetViews(R.id.day_3_name, R.id.day_3_icon, R.id.day_3_temp_max, R.id.day_3_temp_min, R.id.day_3_rain_amount),
                DayWidgetViews(R.id.day_4_name, R.id.day_4_icon, R.id.day_4_temp_max, R.id.day_4_temp_min, R.id.day_4_rain_amount),
                DayWidgetViews(R.id.day_5_name, R.id.day_5_icon, R.id.day_5_temp_max, R.id.day_5_temp_min, R.id.day_5_rain_amount),
                DayWidgetViews(R.id.day_6_name, R.id.day_6_icon, R.id.day_6_temp_max, R.id.day_6_temp_min, R.id.day_6_rain_amount)
            )

            // Query DB & fetch live weather safely in background
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = AppDatabase.getInstance(context)
                    val repo = WeatherRepository(
                        db.weatherDao(),
                        WeatherNetwork.api,
                        WeatherNetwork.geocodingApi,
                        context.applicationContext
                    )

                    val data = repo.ensureWidgetDataReady()
                    if (data != null) {
                        val (city, forecast) = data
                        views.setTextViewText(R.id.widget_location_text, city.name)

                        for (i in 0 until minOf(7, forecast.size)) {
                            val item = forecast[i]
                            val ids = dayContainers[i]

                            val cond = WeatherCodeMapper.map(item.weatherCode)
                            views.setTextViewText(ids.nameId, item.dayNameAr.take(3))
                            views.setImageViewResource(ids.iconId, cond.iconRes)
                            views.setTextViewText(ids.tempMaxId, "${item.tempMax.toInt()}°")
                            views.setTextViewText(ids.tempMinId, "${item.tempMin.toInt()}°")

                            val rainText = if (item.precipitationSum > 0.0) {
                                "${String.format(Locale.US, "%.1f", item.precipitationSum)}mm"
                            } else {
                                "0.0mm"
                            }
                            views.setTextViewText(ids.rainAmountId, rainText)
                        }

                        if (forecast.isNotEmpty()) {
                            try {
                                val chartBitmap = WidgetChartRenderer.createWeeklyChartBitmap(forecast, 480, 110)
                                views.setImageViewBitmap(R.id.widget_rain_chart, chartBitmap)
                            } catch (e: Exception) {
                                Log.e("WeatherWidgetProvider", "Chart generation error: ${e.message}")
                            }
                        }
                    } else {
                        views.setTextViewText(R.id.widget_location_text, "طقس ومطر")
                    }

                    appWidgetManager.updateAppWidget(appWidgetId, views)
                } catch (e: Exception) {
                    Log.e("WeatherWidgetProvider", "updateWidget background error: ${e.message}")
                }
            }
        }
    }
}
