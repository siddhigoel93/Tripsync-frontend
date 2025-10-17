package com.example.tripsync.api

import com.example.tripsync.api.models.LoginResponse
import com.example.tripsync.api.models.LoginRequest
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthService {

    @POST("login")
    suspend fun loginUser(@Body request: LoginRequest): LoginResponse

}
