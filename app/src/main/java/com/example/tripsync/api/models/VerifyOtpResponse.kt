package com.example.tripsync.api.models

data class VerifyOtpResponse(
    val success: Boolean,
    val message: String?,
    val data: Data?
) {
    data class Data(
        val user: User?,
        val tokens: Tokens?
    ) {
        data class User(
            val id: Int,
            val email: String,
            val is_email_verified: Boolean
        )
        data class Tokens(
            val access: String?,
            val refresh: String?
        )
    }
}
