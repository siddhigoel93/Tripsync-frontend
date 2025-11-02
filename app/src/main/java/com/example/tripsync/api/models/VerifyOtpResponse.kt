package com.example.tripsync.api.models

data class VerifyOtpResponse(
    val success: Boolean,
    val message: String?,
    val data: Data?
) {
    data class Data(
        val tokens: Tokens?
    ) {
        data class Tokens(
            val access: String?,
            val refresh: String?
        )
    }
}
