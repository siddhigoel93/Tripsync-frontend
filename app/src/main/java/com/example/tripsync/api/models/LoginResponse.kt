package com.example.tripsync.api.models

data class LoginResponse(
    val status: String?,
    val message: String?,
    val data: LoginData?
)

data class LoginData(
    val user: User?,
    val tokens: Tokens?
)

data class User(
    val id: Int,
    val email: String?,
    val isEmailVerified: Boolean?
)

data class Tokens(
    val access: String?,
    val refresh: String?
)
