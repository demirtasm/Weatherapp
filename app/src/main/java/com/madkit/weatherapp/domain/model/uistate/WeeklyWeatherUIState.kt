package com.madkit.weatherapp.domain.model.uistate

data class WeeklyWeatherUIState (
    val weeklyWeatherCode: List<String> = listOf(),
    val weeklyMaxTemperature: List<Double> = listOf(),
    val weeklyMinTemperature: List<Double> = listOf(),
    val weeklyTimes: List<String> = listOf(),
    val weeklyRelativeHumidity: List<Int> = listOf(),
    val weeklyApparentTemperature: List<Double> = listOf(),

    )