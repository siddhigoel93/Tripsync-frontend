package com.example.tripsync.api.models

data class RegistrationOtpVerifyRequest(
    val email: String,
    val otp: String
)
