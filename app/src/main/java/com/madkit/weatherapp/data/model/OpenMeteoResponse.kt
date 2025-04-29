package com.madkit.weatherapp.data.model

import com.google.gson.annotations.SerializedName

data class OpenMeteoResponse(
    @SerializedName("current")
    val current: CurrentWeather?,
    @SerializedName("hourly")
    val hourly: HourlyData?,
    @SerializedName("daily")
    val daily: DailyData?
) {
    data class CurrentWeather(
        @SerializedName("temperature_2m")
        val temperature_2m: Double
    )
    data class HourlyData(
        @SerializedName("time")
        val time: List<String>,
        @SerializedName("temperature_2m")
        val temperature_2m: List<Double>,
        @SerializedName("precipitation")
        val precipitation: List<Double>,
        @SerializedName("weather_code")
        val weather_code: List<Int>,
        @SerializedName("rain")
        val rain: List<Double>,
        @SerializedName("precipitation_probability")
        val precipitation_probability: List<Int>,
        @SerializedName("wind_speed_10m")
        val wind_speed_10m: List<Double>,
        @SerializedName("wind_direction_10m")
        val wind_direction_10m: List<Double>,
        @SerializedName("is_day")
        val is_day:List<Int>
    )
    data class DailyData(
        @SerializedName("weather_code")
        val weather_code: List<String>,
        @SerializedName("wind_gusts_10m_mean")
        val wind_gusts_10m_mean: List<Double>,
        @SerializedName("time")
        val time: List<String>,
        @SerializedName("uv_index_max")
        val uv_index_max: List<Double>,
        @SerializedName("relative_humidity_2m_mean")
        val relative_humidity_2m_mean: List<Int>,
        @SerializedName("sunrise")
        val sunrise: List<String>,
        @SerializedName("sunset")
        val sunset: List<String>,
        @SerializedName("temperature_2m_max")
        val temperature_2m_max: List<Double>,
        @SerializedName("temperature_2m_min")
        val temperature_2m_min: List<Double>,
        @SerializedName("precipitation_probability_mean")
        val precipitation_probability_mean: List<Int>,
        @SerializedName("temperature_2m_mean")
        val temperature_2m_mean: List<Double>,
        @SerializedName("apparent_temperature_mean")
        val apparent_temperature_mean: List<Double>

    )
}