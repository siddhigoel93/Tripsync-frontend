package com.example.tripsync.api.models

data class LogoutRequest(
    val refresh: String
)

data class LogoutResponse(
    val status: String,
    val message: String,
    val errors: Map<String, List<String>>? = null
)