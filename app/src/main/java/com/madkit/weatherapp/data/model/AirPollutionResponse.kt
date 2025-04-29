package com.madkit.weatherapp.data.model

import com.google.gson.annotations.SerializedName

data class AirPollutionResponse(
    @SerializedName("coord")
    val coord: Coord,
    @SerializedName("list")
    val list: List<AirPollutionData>
){
    data class AirPollutionData(
        @SerializedName("main")
        val main: AQIMain,
        @SerializedName("components")
        val components: AirComponents,
        @SerializedName("dt")
        val dt: Long
    )
    data class Coord (
        @SerializedName("lo")
        val lo: Double,
        @SerializedName("lat")
        val lat:Double)

    data class AQIMain(
        @SerializedName("aqi")
        val aqi: Double
    )
    data class AirComponents(
        @SerializedName("co")
        val co: Double,
        @SerializedName("no")
        val no: Double,
        @SerializedName("no2")
        val no2: Double,
        @SerializedName("o3")
        val o3: Double,
        @SerializedName("so2")
        val so2: Double,
        @SerializedName("pm2_5")
        val pm2_5: Double,
        @SerializedName("pm10")
        val pm10: Double,
        @SerializedName("nh3")
        val nh3: Double
    )
}