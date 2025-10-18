package com.example.tripsync.api.models

data class LoginErrorResponse(
    val status: String?,
    val message: String?,
    val errors: Map<String, List<String>>?
)
