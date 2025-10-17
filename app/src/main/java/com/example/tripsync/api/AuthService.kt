package com.example.tripsync.api

import com.example.tripsync.api.models.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthService {

    @POST("/api/account/register/")
    suspend fun registerUser(@Body request: RegisterRequest): Response<RegisterResponse>

    // Registration OTP verification
    @POST("/api/account/verify-otp/")
    suspend fun verifyOtp(@Body request: RegistrationOtpVerifyRequest): Response<VerifyOtpResponse>

    @POST("/api/account/resend-otp/")
    suspend fun resendOtp(@Body request: EmailRequest): Response<GenericResponse>


    @POST("/api/account/login/")
    suspend fun loginUser(@Body request: LoginRequest): Response<LoginResponse>

    // Send OTP to email
    @POST("/api/account/password/reset/request/")
    suspend fun requestPasswordReset(@Body request: EmailRequest): Response<GenericResponse>

    @POST("/api/account/password/reset/verify/")
    suspend fun verifyPasswordResetOtp(@Body request: ResetPasswordOTPRequest): Response<Any>
}



