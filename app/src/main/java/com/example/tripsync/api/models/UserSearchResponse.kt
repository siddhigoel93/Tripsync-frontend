package com.example.tripsync.api.models


data class UserSearchApiResponse(
    val success: Boolean,
    val message: String,
    val data: UserSearchData
)

data class UserSearchData(
    val count: Int,
    val users: List<UserSearchResponse>
)

data class UserSearchResponse(
    val id: Int,
    val email: String?,
    val fname: String?,
    val lname: String?,
    val phone_number: String?,
    val date: String?,
    val gender: String?,
    val bio: String?,
    val profile_pic_url: String?,
    val bgroup: String?,
    val allergies: String?,
    val medical: String?,
    val ename: String?,
    val enumber: String?,
    val erelation: String?,
    val prefrence: String?
) {
}