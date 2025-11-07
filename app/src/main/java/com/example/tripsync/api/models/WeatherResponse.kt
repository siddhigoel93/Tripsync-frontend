package com.example.tripsync.api.models

data class WeatherResponse(
    val status: String,
    val data: WeatherData,
    val cached: Boolean
)

data class WeatherData(
    val location: String,
    val temperature: Double,
    val wind: Double,
    val chance_of_rain: Int
)
