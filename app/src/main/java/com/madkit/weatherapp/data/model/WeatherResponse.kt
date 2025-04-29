package com.madkit.weatherapp.data.model

import com.google.gson.annotations.SerializedName

data class WeatherResponse(
    @SerializedName("coord")
    val coord: Coord,
    @SerializedName("weather")
    val weather: List<Weather>,
    @SerializedName("base")
    val base: String,
    @SerializedName("main")
    val main: Main,
    @SerializedName("visibility")
    val visibility: Int,
    @SerializedName("wind")
    val wind: Wind,
    @SerializedName("rain")
    val rain: Rain,
    @SerializedName("clouds")
    val clouds: Clouds,
    @SerializedName("dt")
    val dt: Int,
    @SerializedName("sys")
    val sys: Sys,
    @SerializedName("id")
    val id: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("cod")
    val cod: Int
) {
    data class Coord (
        @SerializedName("lo")
        val lo: Double,
        @SerializedName("lat")
        val lat:Double)

    data class Weather(
        @SerializedName("id")
        val id: Int,
        @SerializedName("main")
        val main: String,
        @SerializedName("description")
        val description: String,
        @SerializedName("icon")
        val icon: String)

    data class Main(
        @SerializedName("temp")
        val temp: Double,
        @SerializedName("pressure")
        val pressure: Double,
        @SerializedName("humidity")
        val humidity: Int,
        @SerializedName("temp_min")
        val temp_min: Double,
        @SerializedName("temp_max")
        val temp_max: Double,
        @SerializedName("feels_like")
        val feels_like: Double,
        @SerializedName("sea_level")
        val sea_level: Double,
        @SerializedName("gmd_level")
        val gmd_level: Double,
    )
    data class Wind(
        @SerializedName("speed")
        val speed: Double,
        @SerializedName("deg")
        val deg:Int
    )
    data class Rain(
        @SerializedName("1h") val oneHour: Double?
    )
    data class Clouds(
        @SerializedName("all")
        val all:Int
    )

    data class Sys (
        @SerializedName("type")
        val type:Int,
        @SerializedName("message")
        val message: Double,
        @SerializedName("country")
        val country:String,
        @SerializedName("sunrise")
        val sunrise:Long,
        @SerializedName("sunset")
        val sunset:Long
    )
}