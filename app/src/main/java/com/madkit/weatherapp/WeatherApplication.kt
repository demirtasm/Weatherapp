package com.madkit.weatherapp

import android.app.Application
import com.madkit.weatherapp.network.OpenMeteoService
import com.madkit.weatherapp.network.WeatherService
import com.madkit.weatherapp.repository.WeatherRepository
import com.madkit.weatherapp.viewmodel.LocationViewModel
import com.madkit.weatherapp.viewmodel.WeatherViewModel
import com.madkit.weatherapp.viewmodel.WeatherViewModelFactory

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