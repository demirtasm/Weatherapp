package com.example.weatherapp.network

import com.example.weatherapp.models.AirPollutionResponse
import com.example.weatherapp.models.OpenMeteoResponse
import com.example.weatherapp.models.WeatherResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherService {
    @GET("2.5/weather")
    fun getWeather(
        @Query("lat") lat:Double,
        @Query("lon") lon: Double,
        @Query("units") units: String?,
        @Query("appid") appid:String?,
    ): Call<WeatherResponse>

    @GET("forecast?")
    fun getOpenMeteoWeather(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") current: String,
        @Query("hourly") hourly: String,
        @Query("daily") daily: String,
        @Query("timezone") timezone: String
        ): Call<OpenMeteoResponse>

    @GET("2.5/air_pollution/forecast")
    fun getAirPollutionForecast(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") appid: String
    ): Call<AirPollutionResponse>
}