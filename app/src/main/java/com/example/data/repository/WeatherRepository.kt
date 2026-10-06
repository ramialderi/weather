package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.data.local.CityEntity
import com.example.data.local.DailyForecastEntity
import com.example.data.local.WeatherCacheEntity
import com.example.data.local.WeatherDao
import com.example.data.model.GeocodingResultDto
import com.example.data.model.WeatherApiResponse
import com.example.data.network.GeocodingService
import com.example.data.network.OpenMeteoService
import com.example.ui.model.WeatherCodeMapper
import com.example.widget.WeatherWidgetProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext

class WeatherRepository(
    private val dao: WeatherDao,
    private val api: OpenMeteoService,
    private val geocodingApi: GeocodingService,
    private val context: Context
) {

    val allCities: Flow<List<CityEntity>> = dao.getAllCities()
    val favoriteCities: Flow<List<CityEntity>> = dao.getFavoriteCities()
    val selectedCity: Flow<CityEntity?> = dao.getSelectedCity()

    fun getWeatherCache(cityId: Long): Flow<WeatherCacheEntity?> = dao.getWeatherCache(cityId)
    fun getDailyForecast(cityId: Long): Flow<List<DailyForecastEntity>> = dao.getDailyForecast(cityId)

    suspend fun initializeDefaultCityIfNeeded() = withContext(Dispatchers.IO) {
        val existing = dao.getSelectedCitySuspend()
        if (existing == null) {
            // Seed a popular default city (Riyadh)
            val defaultCityId = dao.insertCity(
                CityEntity(
                    name = "الرياض",
                    country = "المملكة العربية السعودية",
                    latitude = 24.7136,
                    longitude = 46.6753,
                    isFavorite = true,
                    isSelected = true,
                    isCurrentGps = false
                )
            )
            refreshWeatherForCity(defaultCityId, 24.7136, 46.6753)
        }
    }

    suspend fun updateGpsLocation(lat: Double, lon: Double, cityName: String = "الموقع الحالي") = withContext(Dispatchers.IO) {
        dao.clearSelectedCity()
        val existingGps = dao.getCurrentGpsCity()
        val cityId = if (existingGps != null) {
            val updated = existingGps.copy(
                latitude = lat,
                longitude = lon,
                name = cityName,
                isSelected = true,
                updatedAt = System.currentTimeMillis()
            )
            dao.insertCity(updated)
            existingGps.id
        } else {
            dao.insertCity(
                CityEntity(
                    name = cityName,
                    country = "موقعك عبر GPS",
                    latitude = lat,
                    longitude = lon,
                    isFavorite = false,
                    isSelected = true,
                    isCurrentGps = true
                )
            )
        }
        refreshWeatherForCity(cityId, lat, lon)
    }

    suspend fun selectCity(cityId: Long) = withContext(Dispatchers.IO) {
        dao.clearSelectedCity()
        dao.setSelectedCity(cityId)
        val city = dao.getSelectedCitySuspend()
        if (city != null) {
            refreshWeatherForCity(city.id, city.latitude, city.longitude)
        }
    }

    suspend fun addCity(
        name: String,
        country: String,
        latitude: Double,
        longitude: Double,
        selectNow: Boolean
    ): Long = withContext(Dispatchers.IO) {
        if (selectNow) {
            dao.clearSelectedCity()
        }
        val id = dao.insertCity(
            CityEntity(
                name = name,
                country = country,
                latitude = latitude,
                longitude = longitude,
                isFavorite = true,
                isSelected = selectNow,
                isCurrentGps = false
            )
        )
        refreshWeatherForCity(id, latitude, longitude)
        id
    }

    suspend fun toggleFavorite(cityId: Long, isFavorite: Boolean) = withContext(Dispatchers.IO) {
        dao.updateFavorite(cityId, isFavorite)
    }

    suspend fun deleteCity(cityId: Long) = withContext(Dispatchers.IO) {
        dao.deleteCity(cityId)
        val remaining = dao.getSelectedCitySuspend()
        if (remaining == null) {
            val first = dao.getAllCities().firstOrNull()?.firstOrNull()
            if (first != null) {
                selectCity(first.id)
            }
        }
    }

    suspend fun refreshCurrentSelected(): Result<Unit> = withContext(Dispatchers.IO) {
        val city = dao.getSelectedCitySuspend()
            ?: return@withContext Result.failure(Exception("لا توجد مدينة محددة"))
        refreshWeatherForCity(city.id, city.latitude, city.longitude)
    }

    suspend fun refreshWeatherForCity(cityId: Long, lat: Double, lon: Double): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = api.getForecast(
                latitude = lat,
                longitude = lon
            )
            saveForecastResponse(cityId, response)
            // Trigger Widget Update immediately for both 7-day and current-temp widgets
            WeatherWidgetProvider.updateAllWidgets(context)
            com.example.widget.CurrentTempWidgetProvider.updateAllWidgets(context)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("WeatherRepository", "Failed to fetch weather: ${e.message}", e)
            Result.failure(e)
        }
    }

    private suspend fun saveForecastResponse(cityId: Long, response: WeatherApiResponse) {
        val current = response.current
        val daily = response.daily ?: return

        val currentCondition = WeatherCodeMapper.map(current?.weatherCode ?: 0)
        val cache = WeatherCacheEntity(
            cityId = cityId,
            currentTemp = current?.temperature ?: 0.0,
            feelsLike = current?.apparentTemperature ?: 0.0,
            humidity = current?.humidity ?: 0,
            windSpeed = current?.windSpeed ?: 0.0,
            precipitation = current?.precipitation ?: 0.0,
            weatherCode = current?.weatherCode ?: 0,
            conditionText = currentCondition.titleAr,
            uvIndex = daily.uvIndexMax?.firstOrNull() ?: 0.0,
            updatedAt = System.currentTimeMillis()
        )

        val daysCount = minOf(7, daily.time.size)
        val forecastEntities = (0 until daysCount).map { i ->
            val dateStr = daily.time[i]
            val code = daily.weatherCode.getOrElse(i) { 0 }
            val condition = WeatherCodeMapper.map(code)
            val rainProb = daily.precipitationProbabilityMax?.getOrNull(i) ?: 0
            val rainSum = daily.precipitationSum.getOrElse(i) { 0.0 }
            DailyForecastEntity(
                cityId = cityId,
                dayIndex = i,
                date = dateStr,
                dayNameAr = WeatherCodeMapper.formatArabicDay(dateStr, i),
                tempMax = daily.temperatureMax.getOrElse(i) { 0.0 },
                tempMin = daily.temperatureMin.getOrElse(i) { 0.0 },
                precipitationSum = rainSum,
                precipitationProb = rainProb,
                weatherCode = code,
                conditionText = condition.titleAr
            )
        }

        dao.saveWeatherAndForecast(cityId, cache, forecastEntities)
    }

    suspend fun searchCities(query: String): List<GeocodingResultDto> = withContext(Dispatchers.IO) {
        if (query.trim().length < 2) return@withContext emptyList()
        try {
            val response = geocodingApi.searchCities(query.trim())
            response.results ?: emptyList()
        } catch (e: Exception) {
            Log.e("WeatherRepository", "Geocoding search failed: ${e.message}")
            emptyList()
        }
    }

    // Direct synchronous fetch for widget background refresh
    suspend fun syncWidgetDataNow() = withContext(Dispatchers.IO) {
        var city = dao.getSelectedCitySync()
        if (city == null) {
            initializeDefaultCityIfNeeded()
            city = dao.getSelectedCitySync()
        }
        if (city == null) return@withContext
        try {
            val response = api.getForecast(
                latitude = city.latitude,
                longitude = city.longitude
            )
            saveForecastResponse(city.id, response)
        } catch (e: Exception) {
            Log.e("WeatherRepository", "Widget background sync failed: ${e.message}")
        }
    }

    suspend fun ensureWidgetDataReady(): Pair<CityEntity, List<DailyForecastEntity>>? = withContext(Dispatchers.IO) {
        try {
            var city = dao.getSelectedCitySync()
            if (city == null) {
                initializeDefaultCityIfNeeded()
                city = dao.getSelectedCitySync()
            }
            if (city == null) return@withContext null

            var forecast = dao.getDailyForecastSync(city.id)
            if (forecast.isEmpty()) {
                refreshWeatherForCity(city.id, city.latitude, city.longitude)
                forecast = dao.getDailyForecastSync(city.id)
            }
            Pair(city, forecast)
        } catch (e: Exception) {
            Log.e("WeatherRepository", "ensureWidgetDataReady failed: ${e.message}")
            null
        }
    }
}
