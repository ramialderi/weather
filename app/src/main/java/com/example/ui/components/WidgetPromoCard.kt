package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.CityEntity
import com.example.data.local.WeatherCacheEntity
import com.example.ui.model.DailyItemUi
import com.example.ui.model.TempUnit
import com.example.ui.model.WeatherCodeMapper

@Composable
fun WidgetPromoCard(
    city: CityEntity?,
    cache: WeatherCacheEntity?,
    forecastList: List<DailyItemUi>,
    tempUnit: TempUnit,
    canPinWidget: Boolean,
    onPin7DayWidget: () -> Unit,
    onPinCurrentTempWidget: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 0 = 7-Day Summary with Chart, 1 = Current Temperature
    var selectedWidgetTab by remember { mutableIntStateOf(0) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("widget_promo_card"),
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
                        imageVector = Icons.Default.Widgets,
                        contentDescription = null,
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "ويدجت الشاشة الرئيسية (Home Screen)",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Text(
                        text = "مخطط بياني لكميات الأمطار خلال الأسبوع مع درجات الحرارة",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Widget Selector Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0x1F0F172A), RoundedCornerShape(14.dp))
                    .padding(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (selectedWidgetTab == 0) Color(0xFF0284C7) else Color.Transparent)
                        .clickable { selectedWidgetTab = 0 }
                        .padding(vertical = 8.dp)
                        .testTag("tab_7day_widget"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = if (selectedWidgetTab == 0) Color.White else Color(0xFF94A3B8),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "ملخص 7 أيام والمخطط",
                            color = if (selectedWidgetTab == 0) Color.White else Color(0xFF94A3B8),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (selectedWidgetTab == 1) Color(0xFF0284C7) else Color.Transparent)
                        .clickable { selectedWidgetTab = 1 }
                        .padding(vertical = 8.dp)
                        .testTag("tab_current_temp_widget"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Thermostat,
                            contentDescription = null,
                            tint = if (selectedWidgetTab == 1) Color.White else Color(0xFF94A3B8),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "الحرارة الحالية",
                            color = if (selectedWidgetTab == 1) Color.White else Color(0xFF94A3B8),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Widget Previews
            if (selectedWidgetTab == 0) {
                // 7-DAY SUMMARY WIDGET PREVIEW WITH RAINFALL CHART & DAILY RAIN UNDER TEMP
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF1E293B), Color(0xFF0F172A))
                            )
                        )
                        .border(1.dp, Color(0xFF334155), RoundedCornerShape(20.dp))
                        .padding(12.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Header: Title & City & Refresh
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "طقس ومطر • الأسبوع",
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Text(
                                text = city?.name ?: "المدينة",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(Color(0x33334155), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = null,
                                    tint = Color(0xFF94A3B8),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // 7-Day Columns with Day, Icon, High, Low, and Daily Rainfall Amount under them
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            forecastList.take(7).forEach { day ->
                                Column(
                                    modifier = Modifier.weight(1f),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = day.dayNameAr.take(3),
                                        color = if (day.dayIndex == 0) Color(0xFF38BDF8) else Color(0xFFE2E8F0),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Image(
                                        painter = painterResource(id = day.iconRes),
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    // High Temp
                                    Text(
                                        text = "${day.tempMax.toInt()}°",
                                        color = Color(0xFFFBBF24),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    // Low Temp
                                    Text(
                                        text = "${day.tempMin.toInt()}°",
                                        color = Color(0xFF93C5FD),
                                        fontSize = 10.sp
                                    )
                                    // Daily Rain Amount under temp
                                    Text(
                                        text = if (day.rainSumMm > 0.0) "${String.format("%.1f", day.rainSumMm)}mm" else "0.0mm",
                                        color = Color(0xFF38BDF8),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // In-App Preview of the Weekly Rain & Temperature Curve Chart (RTL Aligned with Columns)
                        val days = forecastList.take(7)
                        if (days.isNotEmpty()) {
                            Canvas(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(64.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0x260F172A))
                            ) {
                                val count = days.size
                                val colWidth = size.width / count
                                fun getX(i: Int) = size.width - (i + 0.5f) * colWidth

                                val maxRain = days.maxOfOrNull { it.rainSumMm }?.toFloat()?.coerceAtLeast(3.0f) ?: 5f
                                val minT = days.minOfOrNull { it.tempMin }?.toFloat() ?: 10f
                                val maxT = days.maxOfOrNull { it.tempMax }?.toFloat() ?: 35f
                                val span = (maxT - minT).coerceAtLeast(2f)

                                // 1. Rain filled area from Left to Right (day count-1 down to 0)
                                val rainPath = Path().apply {
                                    moveTo(0f, size.height)
                                    for (i in (count - 1) downTo 0) {
                                        val x = getX(i)
                                        val ratio = (days[i].rainSumMm.toFloat() / maxRain).coerceIn(0f, 1f)
                                        val y = size.height - (ratio * (size.height * 0.72f))
                                        lineTo(x, y)
                                    }
                                    lineTo(size.width, size.height)
                                    close()
                                }
                                drawPath(
                                    path = rainPath,
                                    brush = Brush.verticalGradient(
                                        colors = listOf(Color(0x9938BDF8), Color(0x330284C7), Color(0x050F172A))
                                    )
                                )

                                // 2. Max temp line from Left to Right (day count-1 down to 0)
                                val maxTempPath = Path().apply {
                                    for (i in (count - 1) downTo 0) {
                                        val x = getX(i)
                                        val ratio = (days[i].tempMax.toFloat() - minT) / span
                                        val y = (1f - ratio) * (size.height * 0.45f)
                                        if (i == count - 1) moveTo(x, y) else lineTo(x, y)
                                    }
                                }
                                drawPath(
                                    path = maxTempPath,
                                    color = Color(0xFFFBBF24),
                                    style = Stroke(width = 2.5f, cap = StrokeCap.Round)
                                )

                                // 3. Min temp line from Left to Right (day count-1 down to 0)
                                val minTempPath = Path().apply {
                                    for (i in (count - 1) downTo 0) {
                                        val x = getX(i)
                                        val ratio = (days[i].tempMin.toFloat() - minT) / span
                                        val y = (size.height * 0.45f) + (1f - ratio) * (size.height * 0.4f)
                                        if (i == count - 1) moveTo(x, y) else lineTo(x, y)
                                    }
                                }
                                drawPath(
                                    path = minTempPath,
                                    color = Color(0xFF93C5FD),
                                    style = Stroke(width = 2f, cap = StrokeCap.Round)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = onPin7DayWidget,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("pin_7day_widget_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "إضافة ويدجت المخطط والأمطار للشاشة الرئيسية", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

            } else {
                // CURRENT TEMPERATURE WIDGET PREVIEW
                val currentCondition = WeatherCodeMapper.map(cache?.weatherCode ?: 0)
                val today = forecastList.firstOrNull()

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF1E293B), Color(0xFF0F172A))
                            )
                        )
                        .border(1.dp, Color(0xFF334155), RoundedCornerShape(20.dp))
                        .padding(14.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = city?.name ?: "الطقس المباشر",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .background(Color(0x33334155), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = null,
                                    tint = Color(0xFF94A3B8),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = painterResource(id = currentCondition.iconRes),
                                contentDescription = null,
                                modifier = Modifier.size(44.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (cache != null) tempUnit.format(cache.currentTemp) else "--°",
                                    color = Color.White,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = cache?.conditionText ?: currentCondition.titleAr,
                                    color = Color(0xFF38BDF8),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0x26334155), RoundedCornerShape(10.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (today != null) "عظمى ${today.tempMax.toInt()}° • صغرى ${today.tempMin.toInt()}°" else "عظمى --° • صغرى --°",
                                color = Color(0xFFCBD5E1),
                                fontSize = 11.sp,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = if (today != null && (today.rainProbMax > 0 || today.rainSumMm > 0)) {
                                    if (today.rainSumMm >= 1.0) "💧 ${today.rainSumMm.toInt()}mm" else "💧 ${today.rainProbMax}%"
                                } else "0%",
                                color = Color(0xFF38BDF8),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = onPinCurrentTempWidget,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("pin_current_temp_widget_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "إضافة ويدجت درجة الحرارة الحالية للشاشة", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "💡 يمكنك أيضاً سحب أي من الويدجتين من شاشة هاتفك مباشرة (الضغط المطول على الشاشة > تطبيقات مصغرة Widgets > طقس ومطر).",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
            )
        }
    }
}
