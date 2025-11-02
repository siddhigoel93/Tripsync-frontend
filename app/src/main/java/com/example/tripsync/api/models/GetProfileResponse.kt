package com.example.tripsync.api.models

data class GetProfileResponse(
    val success: Boolean,
    val message: String? = null,
    val data: ProfileData? = null
) {
    data class ProfileData(
        val fname: String? = null,
        val lname: String? = null,
        val phone_number: String? = null,
        val date: String? = null,
        val gender: String? = null,
        val bio: String? = null,
        val bgroup: String? = null,
        val allergies: String? = null,
        val medical: String? = null,
        val ename: String? = null,
        val enumber: String? = null,
        val erelation: String? = null,
        val prefrence: String? = null,
        val picture_url: String? = null
    )
}
