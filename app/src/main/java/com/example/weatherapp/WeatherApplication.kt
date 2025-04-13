package com.example.weatherapp

import android.app.Application
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import com.example.weatherapp.network.OpenMeteoService
import com.example.weatherapp.network.WeatherService
import com.example.weatherapp.repository.WeatherRepository
import com.example.weatherapp.viewmodel.LocationViewModel
import com.example.weatherapp.viewmodel.WeatherViewModel
import com.example.weatherapp.viewmodel.WeatherViewModelFactory

class WeatherApplication : Application() {
    lateinit var weatherViewModel: WeatherViewModel
    lateinit var locationViewModel: LocationViewModel
    lateinit var weatherViewModelFactory: WeatherViewModelFactory


    override fun onCreate() {
        super.onCreate()
        val weatherApi = WeatherService.create()
        val meteoApi = OpenMeteoService.create()
        val repository = WeatherRepository(weatherApi, meteoApi)

        weatherViewModelFactory = WeatherViewModelFactory(repository)
        locationViewModel = LocationViewModel()
    }
}