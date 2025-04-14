package com.madkit.weatherapp.models

data class DailyData(
    val weather_code: List<String>,
    val wind_gusts_10m_mean: List<Double>,
    val time: List<String>,
    val uv_index_max: List<Double>,
    val relative_humidity_2m_mean: List<Int>,
    val sunrise: List<String>,
    val sunset: List<String>,
    val temperature_2m_max: List<Double>,
    val temperature_2m_min: List<Double>,
    val precipitation_probability_mean: List<Int>,
    val temperature_2m_mean: List<Double>,
    val apparent_temperature_mean: List<Double>

)