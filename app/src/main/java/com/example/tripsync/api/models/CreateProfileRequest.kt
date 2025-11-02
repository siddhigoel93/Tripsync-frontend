package com.example.tripsync.api.models

import com.google.gson.annotations.SerializedName

data class CreateProfileRequest(
    @SerializedName("fname") val fname: String,
    @SerializedName("lname") val lname: String,
    @SerializedName("phone_number") val phone_number: String,
    @SerializedName("date") val date: String,
    @SerializedName("gender") val gender: String,
    @SerializedName("bio") val bio: String,

    @SerializedName("bgroup") val bgroup: String,
    @SerializedName("allergies") val allergies: String,
    @SerializedName("medical") val medical: String,

    @SerializedName("ename") val ename: String,
    @SerializedName("enumber") val enumber: String,
    @SerializedName("erelation") val erelation: String,

    // NOTE: backend expects the MIS-SPELLED key:
    @SerializedName("prefrence") val preference: String
)
