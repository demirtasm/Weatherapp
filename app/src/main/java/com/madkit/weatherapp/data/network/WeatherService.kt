package com.madkit.weatherapp.data.network

import com.madkit.weatherapp.data.model.AirPollutionResponse
import com.madkit.weatherapp.data.model.WeatherResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherService {
    @GET("2.5/weather")
    suspend fun getWeather(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("units") units: String?,
        @Query("appid") appid: String?,
    ): WeatherResponse


    @GET("2.5/air_pollution/forecast")
    suspend fun getAirPollutionForecast(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") appid: String
    ): Response<AirPollutionResponse>
}