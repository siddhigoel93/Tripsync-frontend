package com.example.tripsync.api.models

data class User(
    val id: Int,
    val email: String
)

data class Tokens(
    val access: String,
    val refresh: String
)

data class LoginData(
    val user: User,
    val tokens: Tokens
)

data class LoginResponse(
    val status: String,
    val message: String,
    val data: LoginData? // null if login fails
)
