package com.example.tripsync.api.models

data class RegisterResponse(
    val status: String,
    val message: String,
    val data: RegisterData?
)

data class RegisterData(
    val email: String,
    val otp_expires_in: String
)
