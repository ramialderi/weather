package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.CityEntity
import com.example.data.local.WeatherCacheEntity
import com.example.ui.model.DailyItemUi
import com.example.ui.model.TempUnit
import com.example.ui.model.WeatherCodeMapper

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WeatherHeroCard(
    city: CityEntity?,
    cache: WeatherCacheEntity?,
    todayForecast: DailyItemUi?,
    tempUnit: TempUnit,
    modifier: Modifier = Modifier
) {
    val condition = WeatherCodeMapper.map(cache?.weatherCode ?: 0)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("weather_hero_card"),
        shape = RoundedCornerShape(28.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Subtle backdrop art
            Image(
                painter = painterResource(id = R.drawable.weather_hero_1791027067012),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(290.dp)
                    .clip(RoundedCornerShape(28.dp))
            )

            // Dark gradient overlay for perfect readability and modern glass look
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xCC0B132B),
                                Color(0xEE0B132B),
                                Color(0xFA0B132B)
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Location Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "الموقع",
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = city?.name ?: "الطقس المباشر",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    if (city?.isCurrentGps == true) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .background(Color(0x3338BDF8), CircleShape)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "GPS",
                                color = Color(0xFF38BDF8),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                if (!city?.country.isNullOrBlank() && city?.isCurrentGps == false) {
                    Text(
                        text = city?.country ?: "",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF94A3B8))
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Weather Icon & Temp
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Image(
                        painter = painterResource(id = condition.iconRes),
                        contentDescription = condition.titleAr,
                        modifier = Modifier.size(68.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = if (cache != null) tempUnit.format(cache.currentTemp) else "--°",
                            style = MaterialTheme.typography.displayMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        )
                        Text(
                            text = cache?.conditionText ?: condition.titleAr,
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = Color(0xFF7DD3FC),
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                }

                // Today's High / Low & Rain Probability
                if (todayForecast != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "العظمى: ${tempUnit.formatCompact(todayForecast.tempMax)}",
                            color = Color(0xFFFDE047),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(text = " • ", color = Color(0xFF64748B))
                        Text(
                            text = "الصغرى: ${tempUnit.formatCompact(todayForecast.tempMin)}",
                            color = Color(0xFF93C5FD),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        if (todayForecast.rainProbMax > 0 || todayForecast.rainSumMm > 0) {
                            Text(text = " • ", color = Color(0xFF64748B))
                            Text(
                                text = "💧 احتمال الأمطار ${todayForecast.rainProbMax}% (${todayForecast.rainSumMm} ملم)",
                                color = Color(0xFF38BDF8),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Metric Pills in FlowRow
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    maxItemsInEachRow = 3
                ) {
                    WeatherMetricPill(
                        icon = Icons.Default.WaterDrop,
                        label = "هطول الأمطار",
                        value = "${cache?.precipitation ?: 0.0} ملم",
                        color = Color(0xFF38BDF8)
                    )
                    WeatherMetricPill(
                        icon = Icons.Default.Thermostat,
                        label = "المحسوسة",
                        value = if (cache != null) tempUnit.format(cache.feelsLike) else "--",
                        color = Color(0xFFFBBF24)
                    )
                    WeatherMetricPill(
                        icon = Icons.Default.Air,
                        label = "الرياح",
                        value = "${cache?.windSpeed ?: 0.0} كم/س",
                        color = Color(0xFFA5B4FC)
                    )
                    WeatherMetricPill(
                        icon = Icons.Default.WaterDrop,
                        label = "الرطوبة",
                        value = "${cache?.humidity ?: 0}%",
                        color = Color(0xFF6EE7B7)
                    )
                    WeatherMetricPill(
                        icon = Icons.Default.WbSunny,
                        label = "مؤشر UV",
                        value = "${cache?.uvIndex ?: 0.0}",
                        color = Color(0xFFF472B6)
                    )
                }
            }
        }
    }
}

@Composable
private fun WeatherMetricPill(
    icon: ImageVector,
    label: String,
    value: String,
    color: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(Color(0x331E293B), RoundedCornerShape(14.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Column {
            Text(
                text = label,
                color = Color(0xFF94A3B8),
                fontSize = 10.sp
            )
            Text(
                text = value,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
