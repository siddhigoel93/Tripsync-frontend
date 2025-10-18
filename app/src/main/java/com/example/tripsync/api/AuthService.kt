package com.example.tripsync.api

import com.example.tripsync.api.models.LoginResponse
import com.example.tripsync.api.models.LoginRequest
import com.example.tripsync.api.models.EmailRequest
import com.example.tripsync.api.models.GenericResponse
import com.example.tripsync.api.models.OTPVerifyRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthService {

    @POST("/api/account/login/")
    suspend fun loginUser(@Body request: LoginRequest): Response<LoginResponse>

    // Send OTP to email
    @POST("/api/account/password/reset/request/")
    suspend fun requestPasswordReset(@Body request: EmailRequest): Response<GenericResponse>

    @POST("/api/account/password/reset/verify/")
    suspend fun verifyOtp(@Body request: OTPVerifyRequest): Response<Any>

}



