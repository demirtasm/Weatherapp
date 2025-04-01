package com.example.weatherapp.models

data class HourlyWeather(
    val time: String,
    val temperature: Double,
    val precipitation: Int,
    val weatherCode: Int
)