package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.model.DailyItemUi
import com.example.ui.model.TempUnit

@Composable
fun Forecast7DayCard(
    forecastList: List<DailyItemUi>,
    tempUnit: TempUnit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("forecast_7day_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color(0x2638BDF8), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "توقعات الأمطار والحرارة (7 أيام)",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Text(
                        text = "كميات الهطول، الاحتمالية ونطاق درجات الحرارة",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Global week Min and Max to normalize bars
            val weekMin = forecastList.minOfOrNull { it.tempMin } ?: 0.0
            val weekMax = forecastList.maxOfOrNull { it.tempMax } ?: 40.0
            val tempSpan = (weekMax - weekMin).coerceAtLeast(1.0)

            forecastList.forEachIndexed { index, item ->
                ForecastDayRow(
                    item = item,
                    weekMin = weekMin,
                    weekMax = weekMax,
                    tempSpan = tempSpan,
                    tempUnit = tempUnit
                )
                if (index < forecastList.size - 1) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                        thickness = 0.8.dp,
                        modifier = Modifier.padding(vertical = 10.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ForecastDayRow(
    item: DailyItemUi,
    weekMin: Double,
    weekMax: Double,
    tempSpan: Double,
    tempUnit: TempUnit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("day_row_${item.dayIndex}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Day name & date
        Column(
            modifier = Modifier.width(72.dp)
        ) {
            Text(
                text = item.dayNameAr,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (item.dayIndex == 0) FontWeight.Bold else FontWeight.Medium,
                    color = if (item.dayIndex == 0) Color(0xFF38BDF8) else MaterialTheme.colorScheme.onSurface
                )
            )
            Text(
                text = item.date.substringAfter("-"),
                style = MaterialTheme.typography.labelSmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }

        // Weather Icon
        Image(
            painter = painterResource(id = item.iconRes),
            contentDescription = item.conditionTitle,
            modifier = Modifier
                .size(32.dp)
                .padding(end = 4.dp)
        )

        Spacer(modifier = Modifier.width(6.dp))

        // Rain Probability & Amount
        Column(
            modifier = Modifier.width(88.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.WaterDrop,
                    contentDescription = "احتمال المطر",
                    tint = if (item.rainProbMax > 0) Color(0xFF38BDF8) else Color(0xFF64748B),
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = "${item.rainProbMax}%",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (item.rainProbMax >= 50) Color(0xFF38BDF8) else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (item.rainSumMm > 0.0) {
                Text(
                    text = "${item.rainSumMm} ملم",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (item.rainSumMm >= 10.0) Color(0xFF0284C7) else Color(0xFF0EA5E9)
                )
            } else {
                Text(
                    text = "جاف",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }

        Spacer(modifier = Modifier.width(6.dp))

        // Min Temp
        Text(
            text = tempUnit.formatCompact(item.tempMin),
            style = MaterialTheme.typography.labelMedium.copy(
                color = Color(0xFF93C5FD),
                fontWeight = FontWeight.SemiBold
            ),
            modifier = Modifier.width(32.dp)
        )

        // Visual Temp Range Bar
        Box(
            modifier = Modifier
                .weight(1f)
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(Color(0x3364748B))
        ) {
            val startRatio = ((item.tempMin - weekMin) / tempSpan).toFloat().coerceIn(0f, 1f)
            val endRatio = ((item.tempMax - weekMin) / tempSpan).toFloat().coerceIn(0f, 1f)
            val barWidthRatio = (endRatio - startRatio).coerceAtLeast(0.12f)

            Row(modifier = Modifier.fillMaxWidth()) {
                if (startRatio > 0.01f) {
                    Spacer(modifier = Modifier.weight(startRatio))
                }
                Box(
                    modifier = Modifier
                        .weight(barWidthRatio)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF60A5FA),
                                    Color(0xFFFBBF24),
                                    Color(0xFFF97316)
                                )
                            )
                        )
                )
                val remainingRatio = (1f - endRatio).coerceAtLeast(0f)
                if (remainingRatio > 0.01f) {
                    Spacer(modifier = Modifier.weight(remainingRatio))
                }
            }
        }

        Spacer(modifier = Modifier.width(6.dp))

        // Max Temp
        Text(
            text = tempUnit.formatCompact(item.tempMax),
            style = MaterialTheme.typography.labelMedium.copy(
                color = Color(0xFFFBBF24),
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier.width(32.dp)
        )
    }
}
