// Create a new file, e.g., WeatherApiService.kt
package com.example.plantify.data.network

import com.example.plantify.data.WeatherResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {
    @GET("data/2.5/weather")
    suspend fun getCurrentWeather(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric" // Use "metric" for Celsius, "imperial" for Fahrenheit
    ): Response<WeatherResponse>
}