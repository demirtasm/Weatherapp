package com.example.weatherapp.models

data class HourlyData(
    val time: List<String>,
    val temperature_2m: List<Double>,
    val relative_humidity_2m: List<Int>,
    val precipitation: List<Double>,
    val weather_code: List<Int>,
    val temperature_80m: List<Double>,
    val temperature_120m: List<Double>,
    val temperature_180m: List<Double>,
    val rain: List<Double>,
    val precipitation_probability: List<Int>
)