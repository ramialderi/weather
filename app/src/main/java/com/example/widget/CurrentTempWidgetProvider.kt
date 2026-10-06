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
import com.example.WeatherApp
import com.example.ui.model.WeatherCodeMapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CurrentTempWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_REFRESH_CURRENT_TEMP) {
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val app = context.applicationContext as? WeatherApp
                    app?.repository?.syncWidgetDataNow()
                    updateAllWidgets(context)
                } catch (e: Exception) {
                    Log.e("CurrentTempWidget", "Refresh error: ${e.message}")
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }

    companion object {
        const val ACTION_REFRESH_CURRENT_TEMP = "com.example.weatherrain.ACTION_REFRESH_CURRENT_TEMP"

        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val thisWidget = ComponentName(context, CurrentTempWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)
            for (appWidgetId in appWidgetIds) {
                updateWidget(context, appWidgetManager, appWidgetId)
            }
        }

        fun pinWidgetToHomeScreen(context: Context): Boolean {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val appWidgetManager = context.getSystemService(AppWidgetManager::class.java)
                if (appWidgetManager != null && appWidgetManager.isRequestPinAppWidgetSupported) {
                    val provider = ComponentName(context, CurrentTempWidgetProvider::class.java)
                    return appWidgetManager.requestPinAppWidget(provider, null, null)
                }
            }
            return false
        }

        private fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.widget_current_temp)

            // Setup Launch App Intent
            val launchIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingLaunch = PendingIntent.getActivity(
                context,
                2001,
                launchIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_current_root, pendingLaunch)

            // Setup Refresh Intent
            val refreshIntent = Intent(context, CurrentTempWidgetProvider::class.java).apply {
                action = ACTION_REFRESH_CURRENT_TEMP
            }
            val pendingRefresh = PendingIntent.getBroadcast(
                context,
                2002,
                refreshIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_current_refresh, pendingRefresh)

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val app = context.applicationContext as? WeatherApp
                    val db = app?.database ?: return@launch
                    val dao = db.weatherDao()
                    val city = dao.getSelectedCitySync()

                    if (city != null) {
                        val cache = dao.getWeatherCacheSync(city.id)
                        val forecast = dao.getDailyForecastSync(city.id)
                        val today = forecast.firstOrNull()

                        views.setTextViewText(R.id.widget_current_city, city.name)

                        if (cache != null) {
                            views.setTextViewText(R.id.widget_current_temp_val, "${cache.currentTemp.toInt()}°C")
                            val condition = WeatherCodeMapper.map(cache.weatherCode)
                            views.setImageViewResource(R.id.widget_current_icon, condition.iconRes)
                            views.setTextViewText(R.id.widget_current_condition, cache.conditionText)
                        } else {
                            views.setTextViewText(R.id.widget_current_temp_val, "--°C")
                            views.setTextViewText(R.id.widget_current_condition, "جاري التحديث...")
                        }

                        if (today != null) {
                            views.setTextViewText(
                                R.id.widget_current_range,
                                "عظمى ${today.tempMax.toInt()}° • صغرى ${today.tempMin.toInt()}°"
                            )
                            val rainText = if (today.precipitationProb > 0 || today.precipitationSum > 0) {
                                if (today.precipitationSum >= 1.0) "💧 ${today.precipitationSum.toInt()}mm"
                                else "💧 ${today.precipitationProb}%"
                            } else {
                                "جاف 0%"
                            }
                            views.setTextViewText(R.id.widget_current_rain_prob, rainText)
                        } else {
                            views.setTextViewText(R.id.widget_current_range, "عظمى --° • صغرى --°")
                            views.setTextViewText(R.id.widget_current_rain_prob, "0%")
                        }
                    } else {
                        views.setTextViewText(R.id.widget_current_city, "طقس ومطر")
                        views.setTextViewText(R.id.widget_current_condition, "انقر لفتح التطبيق")
                    }

                    appWidgetManager.updateAppWidget(appWidgetId, views)
                } catch (e: Exception) {
                    Log.e("CurrentTempWidget", "Error updating widget: ${e.message}")
                }
            }
        }
    }
}
