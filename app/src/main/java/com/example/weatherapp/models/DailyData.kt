package com.example.weatherapp.models

data class DailyData(
    val weather_code: List<String>,
    val wind_gusts_10m_mean: List<Double>,
    val time: List<String>,
    val uv_index_max: List<Double>,
    val relative_humidity_2m_mean: List<Int>,
    val sunrise: List<String>,
    val sunset: List<String>

)