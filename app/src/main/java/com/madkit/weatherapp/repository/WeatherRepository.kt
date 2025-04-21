package com.madkit.weatherapp.repository

import com.madkit.weatherapp.utils.Constants
import com.madkit.weatherapp.models.AirPollutionResponse
import com.madkit.weatherapp.models.OpenMeteoResponse
import com.madkit.weatherapp.models.WeatherResponse
import com.madkit.weatherapp.network.OpenMeteoService
import com.madkit.weatherapp.network.WeatherService

class WeatherRepository(private val weatherApi: WeatherService, private val meteoApi: OpenMeteoService) {
    suspend fun getOpenMeteoWeather(lat: Double, lon: Double): OpenMeteoResponse? {
        return try {
            val localTimeZone = java.util.TimeZone.getDefault().id
            meteoApi.getOpenMeteoWeather(
                latitude = lat,
                longitude = lon,
                current = "temperature_2m",
                hourly = "temperature_2m,precipitation,weather_code,rain,precipitation_probability,wind_speed_10m,wind_direction_10m,is_day",
                daily = "weather_code,wind_gusts_10m_mean,uv_index_max,relative_humidity_2m_mean,sunrise,sunset,temperature_2m_max,temperature_2m_min,precipitation_probability_mean,temperature_2m_mean,apparent_temperature_mean",
                timezone = localTimeZone
            )
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getAirPollutionForecast(lat: Double, lon: Double): AirPollutionResponse? {
        return try {
            val response = weatherApi.getAirPollutionForecast(lat, lon, Constants.APP_ID)
            if (response.isSuccessful) {
                response.body()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }


    suspend fun getCurrentWeather(lat: Double, lon: Double): WeatherResponse? {
        return try {
            weatherApi.getWeather(lat, lon, Constants.METRIC_UNIT, Constants.APP_ID)
        } catch (e: Exception) {
            null
        }
    }
}