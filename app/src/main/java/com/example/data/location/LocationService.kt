package com.example.data.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.coroutines.resume

data class DetectedCityLocation(
    val cityName: String,
    val countryName: String,
    val latitude: Double,
    val longitude: Double
)

class LocationService(private val context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(context)
    }

    fun hasLocationPermission(): Boolean {
        val fineGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fineGranted || coarseGranted
    }

    @SuppressLint("MissingPermission")
    suspend fun getLastKnownLocation(): Location? {
        if (!hasLocationPermission()) return null
        return try {
            val lastLoc = fusedLocationClient.lastLocation.awaitTask()
            if (lastLoc != null) {
                lastLoc
            } else {
                val cts = CancellationTokenSource()
                fusedLocationClient.getCurrentLocation(
                    Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                    cts.token
                ).awaitTask()
            }
        } catch (e: Exception) {
            Log.e("LocationService", "Error getting location: ${e.message}")
            null
        }
    }

    suspend fun detectCurrentCityLocation(): DetectedCityLocation? = withContext(Dispatchers.IO) {
        val location = getLastKnownLocation() ?: return@withContext null
        val lat = location.latitude
        val lon = location.longitude

        val (city, country) = resolveCityName(lat, lon)
        DetectedCityLocation(
            cityName = city,
            countryName = country,
            latitude = lat,
            longitude = lon
        )
    }

    @Suppress("DEPRECATION")
    private suspend fun resolveCityName(lat: Double, lon: Double): Pair<String, String> = withContext(Dispatchers.IO) {
        try {
            if (Geocoder.isPresent()) {
                val geocoder = Geocoder(context, Locale("ar"))
                val addresses: List<Address>? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    suspendCancellableCoroutine { continuation ->
                        geocoder.getFromLocation(lat, lon, 1, object : Geocoder.GeocodeListener {
                            override fun onGeocode(results: MutableList<Address>) {
                                if (continuation.isActive) continuation.resume(results)
                            }

                            override fun onError(errorMessage: String?) {
                                if (continuation.isActive) continuation.resume(null)
                            }
                        })
                    }
                } else {
                    geocoder.getFromLocation(lat, lon, 1)
                }

                val address = addresses?.firstOrNull()
                if (address != null) {
                    val resolvedCity = address.locality
                        ?: address.subAdminArea
                        ?: address.adminArea
                        ?: address.featureName
                        ?: "موقعي الحالي"
                    val resolvedCountry = address.countryName ?: "موقعك عبر GPS"
                    return@withContext Pair(resolvedCity, resolvedCountry)
                }
            }
        } catch (e: Exception) {
            Log.w("LocationService", "Geocoder resolution failed: ${e.message}")
        }
        Pair("موقعي الحالي", "موقعك عبر GPS")
    }
}

// Backward-compatible alias
typealias LocationHelper = LocationService

private suspend fun <T> Task<T>.awaitTask(): T? = suspendCancellableCoroutine { continuation ->
    addOnSuccessListener { result ->
        if (continuation.isActive) continuation.resume(result)
    }
    addOnFailureListener {
        if (continuation.isActive) continuation.resume(null)
    }
    addOnCanceledListener {
        if (continuation.isActive) continuation.resume(null)
    }
}
