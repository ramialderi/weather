package com.example.ui

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.CitySearchBottomSheet
import com.example.ui.components.Forecast7DayCard
import com.example.ui.components.RainRadarChartCard
import com.example.ui.components.WeatherHeroCard
import com.example.ui.components.WidgetPromoCard
import com.example.ui.model.TempUnit
import com.example.ui.viewmodel.WeatherViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherScreen(
    viewModel: WeatherViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var showSearchSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Location permission launcher
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            viewModel.detectAndDisplayCurrentCityWeather()
        }
    }

    // Auto-prompt location on first launch so user's city is detected automatically
    LaunchedEffect(Unit) {
        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearError()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.statusBars,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0x26334155))
                            .clickable { showSearchSheet = true }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .testTag("top_city_selector")
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = uiState.selectedCity?.name ?: "طقس ومطر",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "تغيير المدينة",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                },
                actions = {
                    // Temperature Unit toggle button (°C / °F)
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color(0x26334155))
                            .clickable { viewModel.toggleTempUnit() }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("temp_unit_toggle"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (uiState.tempUnit == TempUnit.CELSIUS) "°C" else "°F",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF38BDF8),
                            fontSize = 13.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // Search / Add city button
                    IconButton(
                        onClick = { showSearchSheet = true },
                        modifier = Modifier.testTag("search_city_top_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "بحث عن مدينة",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Refresh Button with smooth rotation while updating
                    val infiniteTransition = rememberInfiniteTransition(label = "refresh_spin")
                    val rotation by infiniteTransition.animateFloat(
                        initialValue = 0f,
                        targetValue = 360f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(durationMillis = 800, easing = LinearEasing)
                        ),
                        label = "spin_angle"
                    )

                    IconButton(
                        onClick = { viewModel.refreshWeather() },
                        modifier = Modifier.testTag("refresh_weather_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "تحديث الطقس",
                            tint = if (uiState.isRefreshing) Color(0xFF38BDF8) else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.rotate(if (uiState.isRefreshing) rotation else 0f)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(6.dp))

            if (uiState.isLoading && uiState.weatherCache == null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(
                            color = Color(0xFF38BDF8),
                            strokeWidth = 3.dp
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "جاري تحميل بيانات الطقس والأمطار...",
                            style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF94A3B8))
                        )
                    }
                }
            } else {
                // 1. Weather Hero Card (Current Conditions, Today's High/Low, Rain %, Wind, Humidity)
                val today = uiState.dailyForecast.firstOrNull()
                WeatherHeroCard(
                    city = uiState.selectedCity,
                    cache = uiState.weatherCache,
                    todayForecast = today,
                    tempUnit = uiState.tempUnit
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 2. Widget Preview & 1-tap Pin to Home Screen Card (7-day or Current Temp)
                WidgetPromoCard(
                    city = uiState.selectedCity,
                    cache = uiState.weatherCache,
                    forecastList = uiState.dailyForecast,
                    tempUnit = uiState.tempUnit,
                    canPinWidget = uiState.canPinWidget,
                    onPin7DayWidget = {
                        viewModel.pin7DayWidget()
                        scope.launch {
                            snackbarHostState.showSnackbar("تم إرسال طلب إضافة ويدجت ملخص 7 أيام للشاشة الرئيسية!")
                        }
                    },
                    onPinCurrentTempWidget = {
                        viewModel.pinCurrentTempWidget()
                        scope.launch {
                            snackbarHostState.showSnackbar("تم إرسال طلب إضافة ويدجت درجة الحرارة الحالية للشاشة الرئيسية!")
                        }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 3. 7-Day Forecast (Temperature + Rain) Card (The 7-Day outlook)
                Forecast7DayCard(
                    forecastList = uiState.dailyForecast,
                    tempUnit = uiState.tempUnit
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 4. Rain Radar & Weekly Precipitation Analysis
                RainRadarChartCard(
                    rainSummary = uiState.rainSummary,
                    dailyForecast = uiState.dailyForecast
                )

                Spacer(modifier = Modifier.height(28.dp))
            }
        }
    }

    // City Search & Management Bottom Sheet
    if (showSearchSheet) {
        CitySearchBottomSheet(
            sheetState = sheetState,
            searchQuery = uiState.searchQuery,
            searchResults = uiState.searchResults,
            isSearching = uiState.isSearching,
            savedCities = uiState.allCities,
            selectedCityId = uiState.selectedCity?.id,
            onSearchQueryChange = { viewModel.onSearchQueryChange(it) },
            onClearSearch = { viewModel.clearSearch() },
            onSelectCity = { cityId -> viewModel.selectCity(cityId) },
            onAddPresetCity = { preset ->
                viewModel.addAndSelectCity(preset.name, preset.country, preset.lat, preset.lon)
            },
            onAddSearchResult = { result ->
                val country = listOfNotNull(result.admin1, result.country).joinToString(", ")
                viewModel.addAndSelectCity(result.name, country, result.latitude, result.longitude)
            },
            onToggleFavorite = { cityId, isFav -> viewModel.toggleFavorite(cityId, isFav) },
            onDeleteCity = { cityId -> viewModel.deleteCity(cityId) },
            onRequestGps = {
                locationPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            },
            onDismiss = {
                showSearchSheet = false
                viewModel.clearSearch()
            }
        )
    }
}
