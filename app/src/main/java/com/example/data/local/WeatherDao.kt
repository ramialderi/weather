package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface WeatherDao {

    @Query("SELECT * FROM cities ORDER BY isSelected DESC, isFavorite DESC, updatedAt DESC")
    fun getAllCities(): Flow<List<CityEntity>>

    @Query("SELECT * FROM cities WHERE isFavorite = 1 ORDER BY name ASC")
    fun getFavoriteCities(): Flow<List<CityEntity>>

    @Query("SELECT * FROM cities WHERE isSelected = 1 LIMIT 1")
    fun getSelectedCity(): Flow<CityEntity?>

    @Query("SELECT * FROM cities WHERE isSelected = 1 LIMIT 1")
    suspend fun getSelectedCitySuspend(): CityEntity?

    @Query("SELECT * FROM cities WHERE isSelected = 1 LIMIT 1")
    fun getSelectedCitySync(): CityEntity?

    @Query("SELECT * FROM cities WHERE isCurrentGps = 1 LIMIT 1")
    suspend fun getCurrentGpsCity(): CityEntity?

    @Query("SELECT * FROM weather_cache WHERE cityId = :cityId LIMIT 1")
    fun getWeatherCache(cityId: Long): Flow<WeatherCacheEntity?>

    @Query("SELECT * FROM weather_cache WHERE cityId = :cityId LIMIT 1")
    fun getWeatherCacheSync(cityId: Long): WeatherCacheEntity?

    @Query("SELECT * FROM daily_forecast WHERE cityId = :cityId ORDER BY dayIndex ASC LIMIT 7")
    fun getDailyForecast(cityId: Long): Flow<List<DailyForecastEntity>>

    @Query("SELECT * FROM daily_forecast WHERE cityId = :cityId ORDER BY dayIndex ASC LIMIT 7")
    fun getDailyForecastSync(cityId: Long): List<DailyForecastEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCity(city: CityEntity): Long

    @Query("UPDATE cities SET isSelected = 0")
    suspend fun clearSelectedCity()

    @Query("UPDATE cities SET isSelected = 1 WHERE id = :cityId")
    suspend fun setSelectedCity(cityId: Long)

    @Query("UPDATE cities SET isFavorite = :isFavorite WHERE id = :cityId")
    suspend fun updateFavorite(cityId: Long, isFavorite: Boolean)

    @Query("DELETE FROM cities WHERE id = :cityId")
    suspend fun deleteCity(cityId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeatherCache(cache: WeatherCacheEntity)

    @Query("DELETE FROM daily_forecast WHERE cityId = :cityId")
    suspend fun deleteDailyForecast(cityId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyForecasts(forecasts: List<DailyForecastEntity>)

    @Transaction
    suspend fun saveWeatherAndForecast(
        cityId: Long,
        cache: WeatherCacheEntity,
        forecasts: List<DailyForecastEntity>
    ) {
        insertWeatherCache(cache)
        deleteDailyForecast(cityId)
        insertDailyForecasts(forecasts)
    }
}
