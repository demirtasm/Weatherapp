package com.madkit.weatherapp.domain.model

data class AirPollution(
    val co: Double,
    val no: Double,
    val no2: Double,
    val o3: Double,
    val so2: Double,
    val pm25: Double,
    val pm10: Double,
    val nh3: Double,
    val time: String,
    val aqi: Double
    )