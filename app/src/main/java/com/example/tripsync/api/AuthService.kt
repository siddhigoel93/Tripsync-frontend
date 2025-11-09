package com.example.tripsync.api

import com.example.tripsync.api.models.*
import com.example.tripsync.chatbot.ChatRequest
import com.example.tripsync.chatbot.ChatResponse
import okhttp3.Call
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
import retrofit2.http.Query

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

//    @POST("/api/personal/profile/")
//    suspend fun createProfile(
//        @Header("Authorization") bearer: String,
//        @Body request: CreateProfileRequest
//    ): Response<CreateProfileResponse>

    @Headers("Content-Type: application/json")
    @POST("api/personal/verify-otp/")
    suspend fun verifyPhoneOtp(
        @Body body: OtpCodeRequest
    ): Response<VerifyPhoneResponse>

    @Headers("Content-Type: application/json")
    @POST("api/personal/resend-otp/")
    suspend fun resendPersonalOtp(): Response<ResendOtpResponse>

    @GET("/api/personal/profile/")
    suspend fun getProfile(): Response<GetProfileResponse>


    @PATCH("/api/personal/profile/")
    suspend fun updateProfile(
        @Header("Authorization") bearer: String,
        @Body request: UpdateProfileRequest
    ): Response<UpdateProfileResponse>

//    @Multipart
//    @POST("/api/personal/profile/picture/")
//    suspend fun uploadProfileImage(
//        @Header("Authorization") bearer: String,
//        @Part image: MultipartBody.Part
//    ): Response<GenericResponse>

    @POST("/api/personal/emergency/sos/")
    suspend fun sendEmergencySOS(
        @Header("Authorization") bearer: String,
        @Body request: EmergencySOSRequest
    ): Response<EmergencySOSResponse>


    @GET("api/community/posts/")
    suspend fun listAllPosts(): Response<PostResponse>

    @GET("api/community/posts/{id}/")
    suspend fun getPostDetails(@Path("id") postId: Int): Response<PostDetailResponse>

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

    @POST("/api/community/posts/{id}/like/")
    suspend fun likePost(
        @Path("id") postId: Int,
        @Body requestBody: LikeRequest
    ): Response<LikeResponse>

    @GET("api/community/posts/verify_status/")
    suspend fun checkVerificationStatus(): Response<VerificationStatus>

    data class VerificationStatus(val is_verified: Boolean, val message: String)

    @POST("api/community/posts/{id}/comments/")
    suspend fun addComment(
        @Path("id") postId: Int,
        @Body requestBody: CommentTextRequest
    ): Response<CommentResponse>

    @PATCH("api/community/comments/{id}/update/")
    suspend fun updateComment(
        @Path("id") commentId: Int,
        @Body requestBody: CommentTextRequest
    ): Response<CommentResponse>

    @DELETE("api/community/comments/{id}/delete/")
    suspend fun deleteComment(
        @Path("id") commentId: Int
    ): Response<DeleteResponse>

    @GET("/api/community/posts/")
    suspend fun searchPosts(@Query("search") query: String): Response<SearchPostsResponse>


    @GET("/api/Homepage/weather/")
    suspend fun getWeather(
        @Query("location") location: String
    ): WeatherResponse

    @Multipart
    @POST("/api/personal/profile/")
    suspend fun createProfileWithImage(
        @Header("Authorization") bearer: String,
        @Part("fname") fname: RequestBody,
        @Part("lname") lname: RequestBody,
        @Part("phone_number") phone_number: RequestBody,
        @Part("date") date: RequestBody,
        @Part("gender") gender: RequestBody,
        @Part("bio") bio: RequestBody,
        @Part("bgroup") bgroup: RequestBody,
        @Part("allergies") allergies: RequestBody,
        @Part("medical") medical: RequestBody,
        @Part("ename") ename: RequestBody,
        @Part("enumber") enumber: RequestBody,
        @Part("erelation") erelation: RequestBody,
        @Part("prefrence") preference: RequestBody,
        @Part profile_pic: MultipartBody.Part? = null
    ): Response<CreateProfileResponse>

    @GET("/api/trending/places/")
    suspend fun getTrendingPlaces(): List<TrendingPlace>

    @Multipart
    @PATCH("/api/personal/profile/")
    suspend fun updateProfile(
        @Part("fname") fname: RequestBody?,
        @Part("lname") lname: RequestBody?,
        @Part("date") date: RequestBody?,
        @Part("gender") gender: RequestBody?,
        @Part("bio") bio: RequestBody?,
        @Part profile_pic: MultipartBody.Part?,
        @Part("bgroup") bgroup: RequestBody?,
        @Part("allergies") allergies: RequestBody?,
        @Part("medical") medical: RequestBody?,
        @Part("ename") ename: RequestBody?,
        @Part("enumber") enumber: RequestBody?,
        @Part("erelation") erelation: RequestBody?,
        @Part("prefrence") prefrence: RequestBody?
    ): Response<GetProfileResponse>

    @GET("api/personal/users/")
    suspend fun getAllUsers(): Response<UsersResponse>

    @POST("/api/personal/emergency/sos/")
    suspend fun sendEmergencySos(@Body request: SosRequest): Response<SosResponse>

    @POST("/api/chatbot/")
    suspend fun sendMessage(@Body request: ChatRequest): Response<ChatResponse>

}
