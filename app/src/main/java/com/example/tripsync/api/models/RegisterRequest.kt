package com.example.tripsync.api.models

data class RegisterRequest(
    val email: String,
    val password: String,
    val password2: String
)
