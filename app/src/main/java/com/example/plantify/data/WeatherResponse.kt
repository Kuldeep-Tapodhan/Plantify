// Create a new file, e.g., WeatherResponse.kt
package com.example.plantify.data

data class WeatherResponse(
    val main: Main,
    val weather: List<Weather>,
    val name: String
)

data class Main(
    val temp: Double
)

data class Weather(
    val main: String,
    val description: String,
    val icon: String
)