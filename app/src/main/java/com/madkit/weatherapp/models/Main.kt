package com.madkit.weatherapp.models

import java.io.Serializable

data class Main(
    val temp: Double,
    val pressure: Double,
    val humidity: Int,
    val temp_min: Double,
    val temp_max: Double,
    val feels_like: Double,
    val sea_level: Double,
    val gmd_level: Double,
): Serializable