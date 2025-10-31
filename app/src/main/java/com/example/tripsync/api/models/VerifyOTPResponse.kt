package com.example.tripsync.api.models
data class VerifyOtpResponse(
    val status: String,
    val message: String,
    val data: VerifyOtpData?
)

data class VerifyOtpData(
    val user: UserData,
    val tokens: TokenData
)

data class UserData(
    val id: Int,
    val email: String,
    val is_email_verified: Boolean
)

data class TokenData(
    val refresh: String,
    val access: String
)

