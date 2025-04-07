package com.example.weatherapp.utils

import com.example.weatherapp.R

object WeatherCodeUtils {
    fun getWeatherIconResId(weatherCode: Int): Int {
        return when (weatherCode) {
            0 -> R.drawable.ic_clear_day
            1 -> R.drawable.ic_mainly_clear_day
            2, 3 -> R.drawable.ic_cloudy_day
            45, 48 -> R.drawable.ic_haze_day
            51,61, 80 -> R.drawable.ic_light_rainy_day
            53, 63, 81 -> R.drawable.ic_moderate_rainy_day
            55,65, 82  -> R.drawable.ic_dense_rainy_day
            56 -> R.drawable.ic_light_freezing_rainy_day
            57 -> R.drawable.ic_moderate_freezing_rainy_day
            66, 67 -> R.drawable.ic_freezing_rain_day
            71 -> R.drawable.ic_clear_day
            73 -> R.drawable.ic_clear_day
            75 -> R.drawable.ic_clear_day
            77 -> R.drawable.ic_clear_day
            85 -> R.drawable.ic_clear_day
            86 -> R.drawable.ic_clear_day
            else -> R.drawable.ic_clear_day
        }
    }

     fun getUnit(value: String): String? {
        var value = "°"
        if ("US" == value || "LR" == value || "MM" == value) {
            value = "°F"
        }
        return value
    }
}