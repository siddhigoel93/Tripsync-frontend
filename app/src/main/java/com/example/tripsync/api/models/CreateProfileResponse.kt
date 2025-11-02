package com.example.tripsync.api.models

data class CreateProfileResponse(
    val success: Boolean,
    val message: String? = null,
    val errors: Map<String, List<String>>? = null,
    val data: GetProfileResponse.ProfileData? = null
)
