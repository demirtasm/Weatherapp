package com.madkit.weatherapp.models

data class OpenMeteoResponse( val current: CurrentWeather?,
                              val hourly: HourlyData?,
                              val daily: DailyData?)