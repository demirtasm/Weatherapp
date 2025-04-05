package com.example.weatherapp.models

data class HourlyData(
    val time: List<String>,
    val temperature_2m: List<Double>,
    val precipitation: List<Double>,
    val weather_code: List<Int>,
    val rain: List<Double>,
    val precipitation_probability: List<Int>,
    val wind_speed_10m: List<Double>,
    val wind_direction_10m: List<Double>
)