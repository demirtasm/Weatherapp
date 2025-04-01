package com.example.weatherapp.models

data class DailyData(
    val time: List<String>,
    val temperature_2m_mean: List<Double>
)