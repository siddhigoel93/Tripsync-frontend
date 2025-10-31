package com.example.tripsync.api.models


data class OTPVerifyRequest(
    val email: String,
    val otp: String
)