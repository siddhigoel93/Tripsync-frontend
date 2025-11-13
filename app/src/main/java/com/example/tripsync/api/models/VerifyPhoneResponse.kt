package com.example.tripsync.api.models

import com.google.gson.annotations.SerializedName

data class VerifyPhoneResponse(
    val success: Boolean,
    val message: String?,
    val data: VerifyPhoneData?
)

data class VerifyPhoneData(
    val profile: UserProfile?
)

data class UserProfile(
    val id: Int,
    val email: String?,
    val fname: String?,
    @SerializedName("Iname") val lname: String?,
    @SerializedName("phone_number") val phoneNumber: String?,
    @SerializedName("is_phone_verified") val isPhoneVerified: Boolean?,
    val date: String?,
    val gender: String?,
    val bio: String?,
    @SerializedName("profile_pic") val profilePic: String?,
    @SerializedName("profile_pic_url") val profilePicUrl: String?,
    @SerializedName("bgroup") val bloodGroup: String?,
    val allergies: String?,
    val medical: String?,
    val ename: String?,
    val enumber: String?,
    @SerializedName("erelation") val emergencyRelation: String?,
    @SerializedName("prefrence") val preference: String?,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("updated_at") val updatedAt: String?
)
