package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.ui.WeatherScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.WeatherViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: WeatherViewModel by viewModels {
        val app = application as WeatherApp
        WeatherViewModel.Factory(
            repository = app.repository,
            locationService = app.locationService,
            context = applicationContext
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                WeatherScreen(viewModel = viewModel)
            }
        }
    }
}
