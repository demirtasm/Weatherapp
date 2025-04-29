package com.madkit.weatherapp.domain.repository

import com.madkit.weatherapp.data.model.AirPollutionResponse
import com.madkit.weatherapp.data.model.OpenMeteoResponse
import com.madkit.weatherapp.data.model.WeatherResponse

interface WeatherRepository {

    suspend fun getOpenMeteoWeather(lat: Double, lon: Double): OpenMeteoResponse?
    suspend fun getAirPollutionForecast(lat: Double, lon: Double): AirPollutionResponse?
    suspend fun getCurrentWeather(lat: Double, lon: Double): WeatherResponse?
}