package com.example.plantify.data

data class Scan(
    val id: String,
    val diseaseName: String,
    val date: String,
    val imageUrl: String,
    val isHealthy: Boolean
)
