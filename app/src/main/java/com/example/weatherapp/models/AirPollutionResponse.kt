package com.example.weatherapp.models

data class AirPollutionResponse(
    val coord: Coord,
    val list: List<AirPollutionData>
)