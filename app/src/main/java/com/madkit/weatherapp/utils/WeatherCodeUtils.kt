package com.madkit.weatherapp.utils

import android.content.Context
import com.madkit.weatherapp.R

object WeatherCodeUtils {
    fun getWeatherIconResId(weatherCode: Int, isDay: Boolean): Int {
        return when (weatherCode) {
            0 -> if (isDay) R.drawable.ic_clear_day else R.drawable.ic_clear_night
            1,2,3 -> if (isDay) R.drawable.ic_mainly_clear_day else R.drawable.ic_mainly_clear_night
            45, 48 -> if (isDay) R.drawable.ic_haze_day else R.drawable.ic_haze_night
            51, 61, 80 -> if (isDay) R.drawable.ic_light_rainy_day else R.drawable.ic_light_rainy_night
            53, 63, 81 -> if (isDay) R.drawable.ic_moderate_rainy_day else R.drawable.ic_moderate_rainy_night
            55, 65, 82 -> if (isDay) R.drawable.ic_dense_rainy_day else R.drawable.ic_dense_rainy_night
            56 -> if (isDay) R.drawable.ic_light_freezing_rainy_day else R.drawable.ic_light_freezing_rainy_night
            57 -> if (isDay) R.drawable.ic_moderate_freezing_rainy_day else R.drawable.ic_moderate_freezing_rainy_night
            66, 67 -> if (isDay) R.drawable.ic_moderate_freezing_rainy_day else R.drawable.ic_moderate_freezing_rainy_night
            71, 85, 77 -> if (isDay) R.drawable.ic_light_snow else R.drawable.ic_light_snow
            73 -> if (isDay) R.drawable.ic_moderate_snow else R.drawable.ic_moderate_snow
            75, 86 -> if (isDay) R.drawable.ic_heavy_snow else R.drawable.ic_heavy_snow
            else -> if (isDay) R.drawable.ic_clear_day else R.drawable.ic_clear_night
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