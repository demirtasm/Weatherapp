package com.madkit.weatherapp.network

import com.madkit.weatherapp.utils.Constants
import com.madkit.weatherapp.models.OpenMeteoResponse
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface OpenMeteoService {
    @GET("forecast?")
    suspend fun getOpenMeteoWeather(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") current: String,
        @Query("hourly") hourly: String,
        @Query("daily") daily: String,
        @Query("timezone") timezone: String
    ): OpenMeteoResponse

    companion object {
        fun create(): OpenMeteoService {
            return Retrofit.Builder()
                .baseUrl(Constants.BASE_URL_OPEN_METEO)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(OpenMeteoService::class.java)
        }
    }
}