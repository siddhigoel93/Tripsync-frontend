package com.example.tripsync.api.models

import com.google.gson.annotations.SerializedName

data class DeleteProfileResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String,

    @SerializedName("error_code")
    val errorCode: String? = null
)
