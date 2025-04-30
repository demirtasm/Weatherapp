package com.madkit.weatherapp.domain.repository

import com.madkit.weatherapp.data.model.GeocodingResponse

interface GeocodingRepository {
    suspend fun getGeocoding(lat: Double, lon: Double): GeocodingResponse?
}