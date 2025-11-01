package com.example.tripsync.api

import com.example.tripsync.api.models.*
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part

interface AuthService {

    @POST("/api/account/register/")
    suspend fun registerUser(@Body request: RegisterRequest): Response<RegisterResponse>

    @POST("/api/account/verify-otp/")
    suspend fun verifyOtp(@Body request: RegistrationOtpVerifyRequest): Response<VerifyOtpResponse>

    @POST("/api/account/resend-otp/")
    suspend fun resendOtp(@Body request: EmailRequest): Response<GenericResponse>

    @POST("/api/account/login/")
    suspend fun loginUser(@Body request: LoginRequest): Response<LoginResponse>

    @POST("/api/account/password/reset/request/")
    suspend fun requestPasswordReset(@Body request: EmailRequest): Response<GenericResponse>

    @POST("/api/account/password/reset/verify/")
    suspend fun verifyOtp(@Body request: ResetPasswordOTPRequest): Response<Any>

    @POST("/api/personal/profile/")
    suspend fun createProfile(
        @Header("Authorization") bearer: String,
        @Body request: CreateProfileRequest
    ): Response<CreateProfileResponse>


    @GET("/api/personal/profile/")
    suspend fun getProfile(
        @Header("Authorization") bearer: String
    ): Response<GetProfileResponse>

    @PATCH("/api/personal/profile/")
    suspend fun updateProfile(
        @Header("Authorization") bearer: String,
        @Body request: UpdateProfileRequest
    ): Response<UpdateProfileResponse>

    @Multipart
    @POST("/api/personal/profile/picture/")
    suspend fun uploadProfileImage(
        @Header("Authorization") bearer: String,
        @Part image: MultipartBody.Part
    ): Response<GenericResponse>

    @POST("/api/personal/emergency/sos/")
    suspend fun sendEmergencySOS(
        @Header("Authorization") bearer: String,
        @Body request: EmergencySOSRequest
    ): Response<EmergencySOSResponse>
}
