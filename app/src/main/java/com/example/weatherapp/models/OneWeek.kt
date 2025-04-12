package com.example.weatherapp.models


data class OneWeek(
    val time: String,
    val weatherCode: String,
    val tempMax: Double,
    val tempMin: Double,
    val apparentTemp: Double,
    val humidity: Int
)