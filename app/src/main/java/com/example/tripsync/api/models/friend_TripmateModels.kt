package com.example.tripsync.api.models

import com.google.gson.annotations.SerializedName

data class friend_TripmateSearchResponse(
    val id: Int,
    val email: String?,
    @SerializedName("profile_data")
    val profileData: friend_ProfileData?,
    @SerializedName("is_tripmate")
    val isTripmate: Boolean?,
    @SerializedName("request_status")
    val requestStatus: String?
)

data class friend_ProfileData(
    @SerializedName("full_name")
    val fullName: String?,
    @SerializedName("bio")
    val bio: String?,
    @SerializedName("profile_pic")
    val profilePic: String?,
    @SerializedName("gender")
    val gender: String?,
    @SerializedName("preference")
    val preference: String?
)
