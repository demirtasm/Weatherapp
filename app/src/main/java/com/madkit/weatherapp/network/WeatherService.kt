package com.madkit.weatherapp.network

import com.madkit.weatherapp.Constants
import com.madkit.weatherapp.models.AirPollutionResponse
import com.madkit.weatherapp.models.WeatherResponse
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
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

    companion object {
        fun create(): WeatherService {
            return Retrofit.Builder()
                .baseUrl(Constants.BASE_URL_OPEN_WEATHER)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(WeatherService::class.java)
        }
    }
}