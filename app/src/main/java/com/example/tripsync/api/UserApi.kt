package com.example.tripsync.api

import com.example.tripsync.api.models.User
import com.example.tripsync.api.models.UserSearchResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface UserApi {
    @GET("/api/users/search/")
    suspend fun searchUsers(@Query("q") query: String): Response<List<UserSearchResponse>>
}