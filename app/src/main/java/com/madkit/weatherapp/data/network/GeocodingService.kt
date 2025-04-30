package com.madkit.weatherapp.data.network

import com.madkit.weatherapp.data.model.GeocodingResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface GeocodingService {
    @GET("reverse?")
    suspend fun getGeocoding(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("format") format: String = "json"
    ): GeocodingResponse
}