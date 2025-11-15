package com.example.tripsync.api


import com.example.tripsync.api.models.UserSearchApiResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST


data class UserSearchRequest(
    val fname: String,
    val lname: String
)
interface UserApi {
    @POST("api/personal/users/search/")
    suspend fun searchUsers(@Body request: UserSearchRequest): Response<UserSearchApiResponse>
}