package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.CityEntity
import com.example.data.model.GeocodingResultDto

data class PresetCity(val name: String, val country: String, val lat: Double, val lon: Double)

val POPULAR_CITIES = listOf(
    PresetCity("الرياض", "المملكة العربية السعودية", 24.7136, 46.6753),
    PresetCity("مكة المكرمة", "المملكة العربية السعودية", 21.3891, 39.8579),
    PresetCity("القاهرة", "مصر", 30.0444, 31.2357),
    PresetCity("دبي", "الإمارات العربية المتحدة", 25.2048, 55.2708),
    PresetCity("دمشق", "سوريا", 33.5138, 36.2765),
    PresetCity("عَمّان", "الأردن", 31.9454, 35.9284),
    PresetCity("بغداد", "العراق", 33.3152, 44.3661),
    PresetCity("بيروت", "لبنان", 33.8938, 35.5018),
    PresetCity("القدس", "فلسطين", 31.7683, 35.2137),
    PresetCity("الدوحة", "قطر", 25.2854, 51.5310),
    PresetCity("الكويت", "الكويت", 29.3759, 47.9774),
    PresetCity("لندن", "المملكة المتحدة", 51.5074, -0.1278),
    PresetCity("باريس", "فرنسا", 48.8566, 2.3522),
    PresetCity("إسطنبول", "تركيا", 41.0082, 28.9784)
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CitySearchBottomSheet(
    sheetState: SheetState,
    searchQuery: String,
    searchResults: List<GeocodingResultDto>,
    isSearching: Boolean,
    savedCities: List<CityEntity>,
    selectedCityId: Long?,
    onSearchQueryChange: (String) -> Unit,
    onClearSearch: () -> Unit,
    onSelectCity: (Long) -> Unit,
    onAddPresetCity: (PresetCity) -> Unit,
    onAddSearchResult: (GeocodingResultDto) -> Unit,
    onToggleFavorite: (Long, Boolean) -> Unit,
    onDeleteCity: (Long) -> Unit,
    onRequestGps: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .testTag("city_search_bottom_sheet")
        ) {
            // Title
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "اختيار أو بحث عن مدينة",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier.weight(1f)
                )

                // Current GPS Button
                IconButton(
                    onClick = {
                        onRequestGps()
                        onDismiss()
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color(0x2638BDF8), CircleShape)
                        .testTag("gps_location_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = "موقعي عبر GPS",
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Search input field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("city_search_input"),
                placeholder = { Text("ابحث عن أي مدينة أو عاصمة بالعالم...") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = null)
                },
                trailingIcon = {
                    if (isSearching) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = Color(0xFF38BDF8)
                        )
                    } else if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = onClearSearch) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "مسح")
                        }
                    }
                },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF38BDF8),
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(14.dp))

            // If user is searching and has results
            if (searchResults.isNotEmpty()) {
                Text(
                    text = "نتائج البحث (${searchResults.size}):",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = Color(0xFF38BDF8),
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                ) {
                    items(searchResults) { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    onAddSearchResult(item)
                                    onDismiss()
                                }
                                .padding(vertical = 10.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.name,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = listOfNotNull(item.admin1, item.country).joinToString(", "),
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "إضافة",
                                tint = Color(0xFF38BDF8)
                            )
                        }
                        HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
                    }
                }
            } else {
                // Popular quick cities
                Text(
                    text = "مدن مقترحة سريعة:",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    POPULAR_CITIES.forEach { preset ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0x26334155))
                                .clickable {
                                    onAddPresetCity(preset)
                                    onDismiss()
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = preset.name,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Saved Cities list
                if (savedCities.isNotEmpty()) {
                    Text(
                        text = "مدني المحفوظة:",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                    ) {
                        items(savedCities) { city ->
                            val isSelected = city.id == selectedCityId
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isSelected) Color(0x3338BDF8) else Color.Transparent
                                    )
                                    .clickable {
                                        onSelectCity(city.id)
                                        onDismiss()
                                    }
                                    .padding(vertical = 8.dp, horizontal = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (city.isCurrentGps) Icons.Default.MyLocation else Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = if (isSelected) Color(0xFF38BDF8) else Color(0xFF94A3B8),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = city.name,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color(0xFF38BDF8) else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = city.country,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                IconButton(
                                    onClick = { onToggleFavorite(city.id, city.isFavorite) }
                                ) {
                                    Icon(
                                        imageVector = if (city.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                        contentDescription = "المفضلة",
                                        tint = if (city.isFavorite) Color(0xFFEF4444) else Color(0xFF94A3B8),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                if (savedCities.size > 1 && !isSelected) {
                                    IconButton(
                                        onClick = { onDeleteCity(city.id) }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "حذف",
                                            tint = Color(0xFF94A3B8),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
