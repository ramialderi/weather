package com.example.ui.model

import androidx.annotation.DrawableRes
import com.example.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class WeatherConditionInfo(
    val titleAr: String,
    val titleEn: String,
    @DrawableRes val iconRes: Int,
    val isRainy: Boolean = false,
    val isSunny: Boolean = false,
    val isSnowy: Boolean = false
)

object WeatherCodeMapper {

    fun map(code: Int): WeatherConditionInfo {
        return when (code) {
            0 -> WeatherConditionInfo("صافٍ", "Clear sky", R.drawable.ic_weather_sun, isSunny = true)
            1 -> WeatherConditionInfo("صافٍ غالباً", "Mainly clear", R.drawable.ic_weather_sun, isSunny = true)
            2 -> WeatherConditionInfo("غائم جزئياً", "Partly cloudy", R.drawable.ic_weather_cloud_sun)
            3 -> WeatherConditionInfo("غائم بالكامل", "Overcast", R.drawable.ic_weather_cloud)
            45, 48 -> WeatherConditionInfo("ضباب وضباب جليدي", "Foggy", R.drawable.ic_weather_cloud)
            51, 53, 55 -> WeatherConditionInfo("رذاذ مطري خفيف", "Drizzle", R.drawable.ic_weather_rain, isRainy = true)
            56, 57 -> WeatherConditionInfo("رذاذ متجمد", "Freezing drizzle", R.drawable.ic_weather_rain, isRainy = true)
            61 -> WeatherConditionInfo("أمطار خفيفة", "Slight rain", R.drawable.ic_weather_rain, isRainy = true)
            63 -> WeatherConditionInfo("أمطار معتدلة", "Moderate rain", R.drawable.ic_weather_rain, isRainy = true)
            65 -> WeatherConditionInfo("أمطار غزيرة", "Heavy rain", R.drawable.ic_weather_rain, isRainy = true)
            66, 67 -> WeatherConditionInfo("أمطار متجمدة", "Freezing rain", R.drawable.ic_weather_rain, isRainy = true)
            71, 73, 75 -> WeatherConditionInfo("تساقط ثلوج", "Snowfall", R.drawable.ic_weather_cloud, isSnowy = true)
            77 -> WeatherConditionInfo("حبات ثلجية", "Snow grains", R.drawable.ic_weather_cloud, isSnowy = true)
            80, 81 -> WeatherConditionInfo("زخات مطرية", "Rain showers", R.drawable.ic_weather_rain, isRainy = true)
            82 -> WeatherConditionInfo("زخات مطرية عنيفة", "Violent rain showers", R.drawable.ic_weather_thunder, isRainy = true)
            85, 86 -> WeatherConditionInfo("زخات ثلجية", "Snow showers", R.drawable.ic_weather_cloud, isSnowy = true)
            95 -> WeatherConditionInfo("عواصف رعدية", "Thunderstorm", R.drawable.ic_weather_thunder, isRainy = true)
            96, 99 -> WeatherConditionInfo("عواصف رعدية مصحوبة بالبَرَد", "Thunderstorm with hail", R.drawable.ic_weather_thunder, isRainy = true)
            else -> WeatherConditionInfo("غائم جزئياً", "Partly cloudy", R.drawable.ic_weather_cloud_sun)
        }
    }

    fun formatArabicDay(dateStr: String, dayIndex: Int): String {
        if (dayIndex == 0) return "اليوم"
        if (dayIndex == 1) return "غداً"
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = sdf.parse(dateStr)
            if (date != null) {
                val cal = Calendar.getInstance().apply { time = date }
                when (cal.get(Calendar.DAY_OF_WEEK)) {
                    Calendar.SATURDAY -> "السبت"
                    Calendar.SUNDAY -> "الأحد"
                    Calendar.MONDAY -> "الاثنين"
                    Calendar.TUESDAY -> "الثلاثاء"
                    Calendar.WEDNESDAY -> "الأربعاء"
                    Calendar.THURSDAY -> "الخميس"
                    Calendar.FRIDAY -> "الجمعة"
                    else -> dateStr
                }
            } else {
                dateStr
            }
        } catch (e: Exception) {
            when (dayIndex) {
                2 -> "بعد غد"
                else -> "يوم ${dayIndex + 1}"
            }
        }
    }
}

enum class TempUnit {
    CELSIUS,
    FAHRENHEIT;

    fun format(celsius: Double): String {
        return when (this) {
            CELSIUS -> "${celsius.toInt()}°C"
            FAHRENHEIT -> "${((celsius * 9 / 5) + 32).toInt()}°F"
        }
    }

    fun formatCompact(celsius: Double): String {
        return when (this) {
            CELSIUS -> "${celsius.toInt()}°"
            FAHRENHEIT -> "${((celsius * 9 / 5) + 32).toInt()}°"
        }
    }
}

data class HourlyItemUi(
    val timeLabel: String,
    val tempCelsius: Double,
    val rainProb: Int,
    val rainAmountMm: Double,
    val weatherCode: Int,
    @DrawableRes val iconRes: Int
)

data class DailyItemUi(
    val dayIndex: Int,
    val date: String,
    val dayNameAr: String,
    val tempMax: Double,
    val tempMin: Double,
    val rainSumMm: Double,
    val rainProbMax: Int,
    val weatherCode: Int,
    val conditionTitle: String,
    @DrawableRes val iconRes: Int
)

data class RainSummary(
    val totalRainNext7DaysMm: Double,
    val maxRainDay: String,
    val maxRainAmountMm: Double,
    val rainyDaysCount: Int,
    val hasHeavyRainWarning: Boolean
)
