package com.example.tripsync.api.models

import com.google.gson.annotations.SerializedName

data class VerifyOtpResponse(
    @SerializedName("success")
    val success: Boolean? = null,
    @SerializedName("status")
    val status: String? = null,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("data")
    val data: Data? = null
) {
    data class Data(
        @SerializedName("tokens")
        val tokens: Tokens? = null,
        @SerializedName("user")
        val user: User? = null
    ) {
        data class Tokens(
            @SerializedName("access")
            val access: String? = null,
            @SerializedName("refresh")
            val refresh: String? = null,
            @SerializedName("access_token")
            val access_token: String? = null,
            @SerializedName("refresh_token")
            val refresh_token: String? = null
        )

        data class User(
            @SerializedName("id")
            val id: Int? = null,
            @SerializedName("email")
            val email: String? = null,
            @SerializedName("is_email_verified")
            val isEmailVerified: Boolean? = null
        )
    }
}
