package com.madkit.weatherapp.domain.model.uistate

data class DailyWeatherUIState(
    val weatherCode: String = "",
    val temperature2mMax: String= "",
    val temperature2mMin: String = "",
    val apparentTemperature: String = "",
    val windGusts10mMean: String = "",
    val uvIndex: String = "",
    val humidity: String = "",
    val sunsetTime: String = "",
    val sunriseTime: String = "",
    val rainChange: String = ""

)