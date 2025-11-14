package com.example.tripsync.api.models

import com.google.gson.annotations.SerializedName

data class friend_TripmateSearchResult(
    val id: Int,
    val email: String?,
    @SerializedName("profile_data")
    val profileData: friend_ProfileData?,
    @SerializedName("is_tripmate")
    val isTripmate: Boolean?,
    @SerializedName("request_status")
    val requestStatus: String?
)
