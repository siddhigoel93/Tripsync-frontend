package com.example.tripsync.api.models

import com.google.gson.annotations.SerializedName

data class OtpCodeRequest(
    @SerializedName("otp_code") val otpCode: String
)
