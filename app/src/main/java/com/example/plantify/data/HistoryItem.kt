package com.example.plantify.data

data class HistoryItem(
    val diseaseName: String = "",
    val confidence: Float = 0f,
    val timestamp: Long = 0L,
    val type: String = "" // "Healthy" or "Diseased"
)