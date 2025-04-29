package com.madkit.weatherapp.domain.model.uistate

data class HourlyWeatherUIState (
    val hourlyTemperature: String = "",
    val hourlyAllTemperature: List<Double> = listOf(),
    val hourlyAllWeatherCode: List<Int> = listOf(),
    val hourlyIsDaY: List<Int> = listOf(),
    val hourlyPrecipitationProbability: List<Int> = listOf(),
    val hourlyWindSpeed: List<Double> = listOf(),
    val hourlyWindDirection: List<Double> = listOf(),
)