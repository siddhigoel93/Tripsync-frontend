package com.example.tripsync.api

import com.example.tripsync.api.models.*
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface ProfileService {

    // Create profile (requires token)
//    @POST("/api/personal/profile/")
//    suspend fun createProfile(
//        @Header("Authorization") bearer: String,
//        @Body request: CreateProfileRequest
//    ): Response<CreateProfileResponse>

    @POST("api/personal/verify-otp/")
    suspend fun verifyPhoneOtp(
        @Body body: OtpCodeRequest
    ): Response<VerifyPhoneResponse>

    @POST("api/personal/resend-otp/")
    suspend fun resendPersonalOtp(): Response<ResendOtpResponse>

    // Get profile info (requires token)
//    @GET("/api/personal/profile/")
//    suspend fun getProfile(
//        @Header("Authorization") bearer: String
//    ): Response<GetProfileResponse>

    // Update profile info (requires token)
//    @PATCH("/api/personal/profile/")
//    suspend fun updateProfile(
//        @Header("Authorization") bearer: String,
//        @Body request: UpdateProfileRequest
//    ): Response<UpdateProfileResponse>

    // Upload profile picture (requires token)
//    @Multipart
//    @POST("/api/personal/profile/picture/")
//    suspend fun uploadProfileImage(
//        @Header("Authorization") bearer: String,
//        @Part image: MultipartBody.Part
//    ): Response<GenericResponse>
}
