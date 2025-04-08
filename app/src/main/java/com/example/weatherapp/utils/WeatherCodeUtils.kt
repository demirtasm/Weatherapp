package com.example.weatherapp.utils

import android.content.Context
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
            71, 85,77 -> R.drawable.ic_light_snow
            73-> R.drawable.ic_moderate_snow
            75, 86 -> R.drawable.ic_heavy_snow
            else -> R.drawable.ic_clear_day
        }
    }
    fun getWeatherDescription(context: Context, code: Int): String {
        return when (code) {
            0 -> context.getString(R.string.weather_clear)
            1 -> context.getString(R.string.weather_mainly_clear)
            2 -> context.getString(R.string.weather_partly_cloudy)
            3 -> context.getString(R.string.weather_cloudy)
            45, 48 -> context.getString(R.string.weather_fog)
            51, 61, 80 -> context.getString(R.string.weather_light_rain)
            53, 63, 81 -> context.getString(R.string.weather_moderate_rain)
            55, 65, 82 -> context.getString(R.string.weather_heavy_rain)
            56 -> context.getString(R.string.weather_light_freezing_rain)
            57 -> context.getString(R.string.weather_moderate_freezing_rain)
            66, 67 -> context.getString(R.string.weather_freezing_rain)
            71, 85,77 -> context.getString(R.string.weather_light_snow)
            73 -> context.getString(R.string.weather_moderate_snow)
            75, 86 -> context.getString(R.string.weather_heavy_snow)
            else -> context.getString(R.string.weather_unknown)
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