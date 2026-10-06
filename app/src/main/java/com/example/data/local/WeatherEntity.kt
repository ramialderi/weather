package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cities")
data class CityEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val country: String,
    val latitude: Double,
    val longitude: Double,
    val isFavorite: Boolean = false,
    val isSelected: Boolean = false,
    val isCurrentGps: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "weather_cache")
data class WeatherCacheEntity(
    @PrimaryKey val cityId: Long,
    val currentTemp: Double,
    val feelsLike: Double,
    val humidity: Int,
    val windSpeed: Double,
    val precipitation: Double,
    val weatherCode: Int,
    val conditionText: String,
    val uvIndex: Double,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "daily_forecast")
data class DailyForecastEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val cityId: Long,
    val dayIndex: Int,
    val date: String,
    val dayNameAr: String,
    val tempMax: Double,
    val tempMin: Double,
    val precipitationSum: Double,
    val precipitationProb: Int,
    val weatherCode: Int,
    val conditionText: String
)
