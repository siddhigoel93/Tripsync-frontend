package com.example.tripsync.api.models

data class ItineraryTripCreateRequest(
    val tripname: String,
    val current_loc: String,
    val destination: String,
    val start_date: String,
    val end_date: String,
    val days: Int,
    val trip_type: String,
    val trip_preferences: String,
    val budget: Double
)
