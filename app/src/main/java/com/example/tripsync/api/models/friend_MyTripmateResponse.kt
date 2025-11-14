package com.example.tripsync.api.models

import com.google.gson.annotations.SerializedName

data class friend_MyTripmateResponseItem(
    @SerializedName("id") val id: Int,
    @SerializedName("email") val email: String?,
    @SerializedName("profile_data") val profileData: friend_TripmateProfileData?,
    @SerializedName("is_tripmate") val isTripmate: Boolean?,
    @SerializedName("request_status") val requestStatus: String?
)

data class friend_TripmateProfileData(
    @SerializedName("full_name") val fullName: String?,
    @SerializedName("bio") val bio: String?,
    @SerializedName("profile_pic") val profilePic: String?,
    @SerializedName("gender") val gender: String?,
    @SerializedName("preference") val preference: String?
)
