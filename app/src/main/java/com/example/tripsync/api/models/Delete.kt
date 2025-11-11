package com.example.tripsync.api.models

import com.google.gson.annotations.SerializedName

data class DeleteProfileResponse(
    val success: Boolean,
    val message: String,
    val error_code: String? = null
)