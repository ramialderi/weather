package com.example

import android.app.Application
import com.example.data.local.AppDatabase
import com.example.data.location.LocationService
import com.example.data.network.WeatherNetwork
import com.example.data.repository.WeatherRepository

class WeatherApp : Application() {

    lateinit var database: AppDatabase
        private set

    lateinit var repository: WeatherRepository
        private set

    lateinit var locationService: LocationService
        private set

    val locationHelper: LocationService
        get() = locationService

    override fun onCreate() {
        super.onCreate()
        instance = this
        database = AppDatabase.getInstance(this)
        val api = WeatherNetwork.api
        val geocodingApi = WeatherNetwork.geocodingApi
        repository = WeatherRepository(database.weatherDao(), api, geocodingApi, this)
        locationService = LocationService(this)
    }

    companion object {
        lateinit var instance: WeatherApp
            private set
    }
}
