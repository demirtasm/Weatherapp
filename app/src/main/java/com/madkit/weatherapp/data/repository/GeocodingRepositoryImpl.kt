package com.madkit.weatherapp.data.repository

import com.madkit.weatherapp.data.model.GeocodingResponse
import com.madkit.weatherapp.data.network.GeocodingService
import com.madkit.weatherapp.domain.repository.GeocodingRepository
import javax.inject.Inject

class GeocodingRepositoryImpl@Inject constructor(private val geoCoding: GeocodingService): GeocodingRepository {
    override suspend fun getGeocoding(
        lat: Double,
        lon: Double
    ): GeocodingResponse? {
        return try {
            geoCoding.getGeocoding(
                lat = lat,
                lon = lon
            )
        }catch (e: Exception) {
            null
        }
    }

}