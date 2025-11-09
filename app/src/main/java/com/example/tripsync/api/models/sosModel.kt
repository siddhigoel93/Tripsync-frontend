package com.example.tripsync.api.models

data class SosRequest(
    val message: String?, // Nullable since the user might not enter a custom message
    val location: String?
)
// In com.example.tripsync.api.models.SosResponse.kt
data class SosResponse(
    val success: Boolean,
    val message: String,
    val data: SosData? // Null if success is false
)

// In com.example.tripsync.api.models.SosData.kt
data class SosData(
    val sent_to: String,
    val contact_name: String,
    val relation: String
)