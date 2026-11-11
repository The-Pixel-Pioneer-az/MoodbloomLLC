package com.example.model

data class TruckLocation(
    val id: String,
    val name: String,
    val address: String,
    val cityState: String,
    val scheduleDay: String,
    val hours: String,
    val isCurrent: Boolean = false,
    val distanceMiles: Double,
    val waitTimeMinutes: Int = 12,
    val parkingTip: String = "Street parking and designated food park lot available.",
    val latitude: Double = 34.0353,
    val longitude: Double = -118.2325
)
