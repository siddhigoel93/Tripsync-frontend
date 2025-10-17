package com.example.tripsync.api.models

data class OTPVerifyRequest(
    val email: String,
    val otp: String,
    val new_password: String,
    val confirm_password: String
)
