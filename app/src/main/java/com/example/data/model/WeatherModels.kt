package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class WeatherApiResponse(
    @Json(name = "latitude") val latitude: Double,
    @Json(name = "longitude") val longitude: Double,
    @Json(name = "timezone") val timezone: String?,
    @Json(name = "current") val current: CurrentWeatherDto?,
    @Json(name = "daily") val daily: DailyWeatherDto?,
    @Json(name = "hourly") val hourly: HourlyWeatherDto?
)

@JsonClass(generateAdapter = true)
data class CurrentWeatherDto(
    @Json(name = "time") val time: String,
    @Json(name = "temperature_2m") val temperature: Double,
    @Json(name = "relative_humidity_2m") val humidity: Int,
    @Json(name = "apparent_temperature") val apparentTemperature: Double,
    @Json(name = "is_day") val isDay: Int,
    @Json(name = "precipitation") val precipitation: Double,
    @Json(name = "weather_code") val weatherCode: Int,
    @Json(name = "wind_speed_10m") val windSpeed: Double
)

@JsonClass(generateAdapter = true)
data class DailyWeatherDto(
    @Json(name = "time") val time: List<String>,
    @Json(name = "weather_code") val weatherCode: List<Int>,
    @Json(name = "temperature_2m_max") val temperatureMax: List<Double>,
    @Json(name = "temperature_2m_min") val temperatureMin: List<Double>,
    @Json(name = "precipitation_sum") val precipitationSum: List<Double>,
    @Json(name = "precipitation_probability_max") val precipitationProbabilityMax: List<Int>?,
    @Json(name = "uv_index_max") val uvIndexMax: List<Double>?,
    @Json(name = "wind_speed_10m_max") val windSpeedMax: List<Double>?
)

@JsonClass(generateAdapter = true)
data class HourlyWeatherDto(
    @Json(name = "time") val time: List<String>,
    @Json(name = "temperature_2m") val temperature: List<Double>,
    @Json(name = "precipitation_probability") val precipitationProbability: List<Int>?,
    @Json(name = "precipitation") val precipitation: List<Double>?,
    @Json(name = "weather_code") val weatherCode: List<Int>?
)

@JsonClass(generateAdapter = true)
data class GeocodingResponse(
    @Json(name = "results") val results: List<GeocodingResultDto>?
)

@JsonClass(generateAdapter = true)
data class GeocodingResultDto(
    @Json(name = "id") val id: Long,
    @Json(name = "name") val name: String,
    @Json(name = "latitude") val latitude: Double,
    @Json(name = "longitude") val longitude: Double,
    @Json(name = "country") val country: String?,
    @Json(name = "admin1") val admin1: String?
)
