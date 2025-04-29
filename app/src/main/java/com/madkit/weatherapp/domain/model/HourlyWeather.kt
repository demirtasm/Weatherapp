package com.madkit.weatherapp.domain.model

data class HourlyWeather(
    val time: String,
    val temperature: Double,
    val precipitation: Int,
    val weatherCode: Int,
    val isDay: Boolean
)