package com.example.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.CityEntity
import com.example.data.local.WeatherCacheEntity
import com.example.data.location.LocationService
import com.example.data.model.GeocodingResultDto
import com.example.data.repository.WeatherRepository
import com.example.ui.model.DailyItemUi
import com.example.ui.model.RainSummary
import com.example.ui.model.TempUnit
import com.example.ui.model.WeatherCodeMapper
import com.example.widget.CurrentTempWidgetProvider
import com.example.widget.WeatherWidgetProvider
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class WeatherUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val selectedCity: CityEntity? = null,
    val weatherCache: WeatherCacheEntity? = null,
    val dailyForecast: List<DailyItemUi> = emptyList(),
    val rainSummary: RainSummary? = null,
    val allCities: List<CityEntity> = emptyList(),
    val favoriteCities: List<CityEntity> = emptyList(),
    val searchResults: List<GeocodingResultDto> = emptyList(),
    val isSearching: Boolean = false,
    val searchQuery: String = "",
    val errorMessage: String? = null,
    val tempUnit: TempUnit = TempUnit.CELSIUS,
    val canPinWidget: Boolean = false,
    val isDetectingLocation: Boolean = false
)

class WeatherViewModel(
    private val repository: WeatherRepository,
    private val locationService: LocationService,
    private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        WeatherUiState(canPinWidget = WeatherWidgetProvider.canPinWidget(context))
    )
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null
    private var forecastJob: Job? = null
    private var weatherCacheJob: Job? = null

    init {
        viewModelScope.launch {
            repository.initializeDefaultCityIfNeeded()
            // Automatically detect user's current city and display its weather if permission is granted
            if (locationService.hasLocationPermission()) {
                detectAndDisplayCurrentCityWeather()
            }
        }
        observeCities()
        observeSelectedCity()
    }

    private fun observeCities() {
        viewModelScope.launch {
            repository.allCities.collectLatest { cities ->
                _uiState.update { it.copy(allCities = cities) }
            }
        }
        viewModelScope.launch {
            repository.favoriteCities.collectLatest { favs ->
                _uiState.update { it.copy(favoriteCities = favs) }
            }
        }
    }

    private fun observeSelectedCity() {
        viewModelScope.launch {
            repository.selectedCity.collectLatest { city ->
                _uiState.update { it.copy(selectedCity = city) }
                if (city != null) {
                    observeWeatherForCity(city.id)
                } else {
                    _uiState.update { it.copy(isLoading = false) }
                }
            }
        }
    }

    private fun observeWeatherForCity(cityId: Long) {
        weatherCacheJob?.cancel()
        weatherCacheJob = viewModelScope.launch {
            repository.getWeatherCache(cityId).collectLatest { cache ->
                _uiState.update {
                    it.copy(
                        weatherCache = cache,
                        isLoading = if (cache != null) false else it.isLoading
                    )
                }
            }
        }

        forecastJob?.cancel()
        forecastJob = viewModelScope.launch {
            repository.getDailyForecast(cityId).collectLatest { list ->
                val dailyItems = list.map { entity ->
                    val condition = WeatherCodeMapper.map(entity.weatherCode)
                    DailyItemUi(
                        dayIndex = entity.dayIndex,
                        date = entity.date,
                        dayNameAr = entity.dayNameAr,
                        tempMax = entity.tempMax,
                        tempMin = entity.tempMin,
                        rainSumMm = entity.precipitationSum,
                        rainProbMax = entity.precipitationProb,
                        weatherCode = entity.weatherCode,
                        conditionTitle = condition.titleAr,
                        iconRes = condition.iconRes
                    )
                }

                // Compute Rain summary
                val totalRain = dailyItems.sumOf { it.rainSumMm }
                val maxRainDay = dailyItems.maxByOrNull { it.rainSumMm }
                val rainyDays = dailyItems.count { it.rainSumMm > 0.5 || it.rainProbMax >= 40 }
                val hasWarning = dailyItems.any { it.rainSumMm >= 15.0 || it.weatherCode in listOf(65, 82, 95, 96, 99) }

                val summary = if (dailyItems.isNotEmpty()) {
                    RainSummary(
                        totalRainNext7DaysMm = totalRain,
                        maxRainDay = maxRainDay?.dayNameAr ?: "اليوم",
                        maxRainAmountMm = maxRainDay?.rainSumMm ?: 0.0,
                        rainyDaysCount = rainyDays,
                        hasHeavyRainWarning = hasWarning
                    )
                } else null

                _uiState.update {
                    it.copy(
                        dailyForecast = dailyItems,
                        rainSummary = summary,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun refreshWeather() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true, errorMessage = null) }
            val result = repository.refreshCurrentSelected()
            _uiState.update {
                it.copy(
                    isRefreshing = false,
                    errorMessage = if (result.isFailure) "تعذر تحديث الطقس، تحقق من الاتصال بالإنترنت" else null
                )
            }
        }
    }

    fun selectCity(cityId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.selectCity(cityId)
        }
    }

    fun addAndSelectCity(name: String, country: String, lat: Double, lon: Double) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, searchQuery = "", searchResults = emptyList()) }
            repository.addCity(name, country, lat, lon, selectNow = true)
        }
    }

    fun toggleFavorite(cityId: Long, currentIsFavorite: Boolean) {
        viewModelScope.launch {
            repository.toggleFavorite(cityId, !currentIsFavorite)
        }
    }

    fun deleteCity(cityId: Long) {
        viewModelScope.launch {
            repository.deleteCity(cityId)
        }
    }

    fun toggleTempUnit() {
        _uiState.update {
            val next = if (it.tempUnit == TempUnit.CELSIUS) TempUnit.FAHRENHEIT else TempUnit.CELSIUS
            it.copy(tempUnit = next)
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        searchJob?.cancel()
        if (query.trim().length < 2) {
            _uiState.update { it.copy(searchResults = emptyList(), isSearching = false) }
            return
        }
        searchJob = viewModelScope.launch {
            _uiState.update { it.copy(isSearching = true) }
            delay(400) // debounce
            val results = repository.searchCities(query)
            _uiState.update { it.copy(searchResults = results, isSearching = false) }
        }
    }

    fun clearSearch() {
        _uiState.update { it.copy(searchQuery = "", searchResults = emptyList(), isSearching = false) }
    }

    /**
     * Uses LocationService and FusedLocationProviderClient to detect the user's
     * current city and display its weather automatically.
     */
    fun detectAndDisplayCurrentCityWeather() {
        viewModelScope.launch {
            if (!locationService.hasLocationPermission()) {
                _uiState.update { it.copy(errorMessage = "يرجى منح إذن الموقع أولاً لتحديد مدينتك تلقائياً") }
                return@launch
            }
            _uiState.update { it.copy(isDetectingLocation = true, isLoading = true, errorMessage = null) }
            val detected = locationService.detectCurrentCityLocation()
            if (detected != null) {
                repository.updateGpsLocation(
                    lat = detected.latitude,
                    lon = detected.longitude,
                    cityName = detected.cityName
                )
                _uiState.update { it.copy(isDetectingLocation = false) }
            } else {
                _uiState.update {
                    it.copy(
                        isDetectingLocation = false,
                        isLoading = false,
                        errorMessage = "تعذر تحديد المدينة الحالية عبر GPS"
                    )
                }
            }
        }
    }

    fun requestGpsLocation() {
        detectAndDisplayCurrentCityWeather()
    }

    fun pin7DayWidget() {
        WeatherWidgetProvider.pinWidgetToHomeScreen(context)
    }

    fun pinCurrentTempWidget() {
        CurrentTempWidgetProvider.pinWidgetToHomeScreen(context)
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    class Factory(
        private val repository: WeatherRepository,
        private val locationService: LocationService,
        private val context: Context
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return WeatherViewModel(repository, locationService, context) as T
        }
    }
}
