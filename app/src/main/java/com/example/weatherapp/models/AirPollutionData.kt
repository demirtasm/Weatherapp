package com.example.weatherapp.models

data class AirPollutionData(
    val main: AQIMain,
    val components: AirComponents,
    val dt: Long
)