package com.example.tripsync.api

import com.example.tripsync.api.models.*
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface ProfileService {

    @POST("api/personal/profile/")
    suspend fun createProfile(
        @Body request: CreateProfileRequest
    ): Response<CreateProfileResponse>


    @POST("api/personal/verify-otp/")
    suspend fun verifyPhoneOtp(
        @Body body: OtpCodeRequest
    ): Response<VerifyPhoneResponse>

    @POST("api/personal/resend-otp/")
    suspend fun resendPersonalOtp(): Response<ResendOtpResponse>

    @GET("api/personal/profile/")
    suspend fun getProfile(): Response<GetProfileResponse>

    @PATCH("api/personal/profile/")
    suspend fun updateProfile(
        @Body request: UpdateProfileRequest
    ): Response<UpdateProfileResponse>


    @Multipart
    @POST("api/personal/profile/picture/")
    suspend fun uploadProfileImage(
        @Part image: MultipartBody.Part
    ): Response<GenericResponse>
}