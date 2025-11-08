package com.example.tripsync.api.models

data class UsersResponse(
    val success: Boolean,
    val message: String,
    val data: UsersData?
)

data class UsersData(
    val users: List<UserS>?,
    val count: Int
)

data class UserS(
    val id: Int,
    val fname: String,
    val lname: String
) {
    val fullName: String
        get() = "$fname $lname"
}
