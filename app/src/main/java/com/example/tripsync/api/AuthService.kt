package com.example.tripsync.api

import com.example.tripsync.api.models.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

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

    @Headers("Content-Type: application/json")
    @POST("api/personal/verify-otp/")
    suspend fun verifyPhoneOtp(
        @Body body: OtpCodeRequest
    ): Response<VerifyPhoneResponse>

    @Headers("Content-Type: application/json")
    @POST("api/personal/resend-otp/")
    suspend fun resendPersonalOtp(): Response<ResendOtpResponse>

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


    @GET("api/community/posts/")
    suspend fun listAllPosts(): Response<PostListResponse>

    @GET("api/community/posts/{id}/")
    suspend fun getPostDetails(@Path("id") postId: Int): Response<Post>

    @POST("api/community/posts/")
    suspend fun createPost(@Body postData: Post): Post

    @DELETE("api/community/posts/{id}/delete/")
    suspend fun deletePost(@Path("id") postId: Int): Response<Unit>

    @GET("api/community/posts/my_posts/")
    suspend fun getMyPosts(): Response<List<Post>>

    @Multipart
    @POST("api/community/posts/create/")
    suspend fun createPost(
        @Part("title") title: RequestBody,
        @Part("desc") desc: RequestBody,
        @Part("loc") loc: RequestBody,
        @Part("loc_rating") loc_rating: Int,

        @Part img: MultipartBody.Part?,
        @Part vid: MultipartBody.Part?
    ): Response<Post>

    @GET("api/community/posts/verify_status/")
    suspend fun checkVerificationStatus(): Response<VerificationStatus>

    data class VerificationStatus(val is_verified: Boolean, val message: String)
}
